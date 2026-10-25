package io.github.movetakeitblue.iitcexpressive.manager

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import iitc_expressive.composeapp.generated.resources.Res
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.*
import io.github.movetakeitblue.iitcexpressive.bridge.BridgeScripts
import io.github.movetakeitblue.iitcexpressive.model.Channel
import io.github.movetakeitblue.iitcexpressive.model.Plugin
import io.github.movetakeitblue.iitcexpressive.storage.AppStorage

class IITCManager(
    val storage: AppStorage,
    val httpClient: HttpClient = HttpClient()
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _currentChannel = MutableStateFlow(Channel.RELEASE)
    val currentChannel: StateFlow<Channel> = _currentChannel.asStateFlow()

    private val _customChannelUrl = MutableStateFlow("")
    val customChannelUrl: StateFlow<String> = _customChannelUrl.asStateFlow()

    private val _plugins = MutableStateFlow<Map<String, Plugin>>(emptyMap())
    val plugins: StateFlow<Map<String, Plugin>> = _plugins.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(listOf("All"))
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _core = MutableStateFlow<Plugin?>(null)
    val core: StateFlow<Plugin?> = _core.asStateFlow()

    private val _isCheckingUpdates = MutableStateFlow(false)
    val isCheckingUpdates: StateFlow<Boolean> = _isCheckingUpdates.asStateFlow()

    private val _lastCheckTime = MutableStateFlow<Long?>(null)
    val lastCheckTime: StateFlow<Long?> = _lastCheckTime.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun startup(fallbackCoreCode: String? = null) {
        val channelStr = storage.getString("channel", Channel.RELEASE.key)
        val channel = Channel.fromKey(channelStr)
        _currentChannel.value = channel
        _customChannelUrl.value = storage.getString("custom_channel_url", "")
        val lastCheck = storage.getLong("last_check_update", 0L)
        if (lastCheck > 0) _lastCheckTime.value = lastCheck

        refreshPluginsView()

        scope.launch {
            seedBundledResourcesIfNeeded(fallbackCoreCode)
            checkUpdates(force = false)
        }
    }

    private suspend fun seedBundledResourcesIfNeeded(fallbackCoreCode: String? = null) = withContext(Dispatchers.Default) {
        var needsRefresh = false
        val ch = _currentChannel.value

        // 1. Seed fallback core if needed
        val existingCore = storage.getString("${ch.key}_iitc_core", "")
        if (existingCore.isBlank()) {
            val coreCode = fallbackCoreCode ?: try {
                Res.readBytes("files/total-conversion-build.user.js").decodeToString()
            } catch (e: Exception) {
                println("Failed to read bundled total-conversion-build.user.js: ${e.message}")
                null
            }
            if (!coreCode.isNullOrBlank()) {
                val parsed = PluginParser.parseMeta(coreCode)
                if (parsed != null) {
                    saveCore(ch, parsed.copy(code = coreCode))
                    needsRefresh = true
                }
            }
        }

        // 2. Seed fallback plugins catalog if needed
        val existingCatalog = storage.getString("${ch.key}_plugins_catalog", "").removePrefix("\uFEFF").trim()
        if (existingCatalog.isBlank() || existingCatalog == "{}") {
            try {
                val catalogJson = Res.readBytes("files/plugins_catalog.json").decodeToString().removePrefix("\uFEFF").trim()
                if (catalogJson.isNotBlank()) {
                    storage.setString("${ch.key}_plugins_catalog", catalogJson)
                    needsRefresh = true
                }
            } catch (e: Exception) {
                println("Failed to read bundled plugins_catalog.json: ${e.message}")
            }
        }

        if (needsRefresh) {
            refreshPluginsView()
        }
    }

    fun setChannel(channel: Channel) {
        _currentChannel.value = channel
        storage.setString("channel", channel.key)
        refreshPluginsView()
        scope.launch {
            checkUpdates(force = true)
        }
    }

    fun setCustomChannelUrl(url: String) {
        _customChannelUrl.value = url
        storage.setString("custom_channel_url", url)
        if (_currentChannel.value == Channel.CUSTOM) {
            scope.launch { checkUpdates(force = true) }
        }
    }

    fun getChannelHost(channel: Channel): String {
        return when (channel) {
            Channel.RELEASE -> channel.defaultHost
            Channel.BETA -> channel.defaultHost
            Channel.CUSTOM -> _customChannelUrl.value.ifBlank { channel.defaultHost }
        }
    }

    fun refreshPluginsView() {
        val ch = _currentChannel.value.key
        val catalogJson = storage.getString("${ch}_plugins_catalog", "{}").removePrefix("\uFEFF").trim()
        val catalogMap = mutableMapOf<String, Plugin>()
        try {
            val rootObj = json.parseToJsonElement(catalogJson).jsonObject
            for ((key, elem) in rootObj) {
                if (elem is JsonObject) {
                    try {
                        val p = json.decodeFromJsonElement<Plugin>(elem)
                        catalogMap[p.uid.ifBlank { key }] = p
                    } catch (pe: Exception) {
                        println("Failed to decode plugin $key: ${pe.message}")
                    }
                }
            }
        } catch (e: Exception) {
            println("refreshPluginsView parse error: ${e.message}")
        }

        // Local downloaded code
        val localCodesJson = storage.getString("${ch}_plugins_local", "{}")
        val localCodes = mutableMapOf<String, String>()
        try {
            val codesObj = json.parseToJsonElement(localCodesJson).jsonObject
            for ((key, elem) in codesObj) {
                if (elem is JsonPrimitive && elem.isString) {
                    localCodes[key] = elem.content
                }
            }
        } catch (e: Exception) {
            // Ignore
        }

        // User custom plugins
        val userPluginsJson = storage.getString("plugins_user", "{}")
        val userPluginsMap = mutableMapOf<String, Plugin>()
        try {
            val userObj = json.parseToJsonElement(userPluginsJson).jsonObject
            for ((key, elem) in userObj) {
                if (elem is JsonObject) {
                    val p = json.decodeFromJsonElement<Plugin>(elem)
                    userPluginsMap[p.uid.ifBlank { key }] = p
                }
            }
        } catch (e: Exception) {
            // Ignore
        }

        // Plugin state (on/off)
        val stateJson = storage.getString("plugins_state", "{}")
        val stateMap = mutableMapOf<String, String>()
        try {
            val stateObj = json.parseToJsonElement(stateJson).jsonObject
            for ((key, elem) in stateObj) {
                if (elem is JsonPrimitive && elem.isString) {
                    stateMap[key] = elem.content
                }
            }
        } catch (e: Exception) {
            // Ignore
        }

        // Merge plugins
        val merged = mutableMapOf<String, Plugin>()

        for ((uid, plugin) in catalogMap) {
            val userOverride = userPluginsMap[uid]
            val status = stateMap[uid] ?: plugin.status
            val code = userOverride?.code ?: localCodes[uid] ?: plugin.code
            merged[uid] = if (userOverride != null) {
                userOverride.copy(status = status, override = true, code = code)
            } else {
                plugin.copy(status = status, code = code)
            }
        }

        for ((uid, userPlugin) in userPluginsMap) {
            if (!merged.containsKey(uid)) {
                val status = stateMap[uid] ?: "on"
                merged[uid] = userPlugin.copy(status = status, user = true)
            }
        }

        _plugins.value = merged

        // Categories
        val cats = sortedSetOf("All")
        for (p in merged.values) {
            if (p.category.isNotBlank()) cats.add(p.category)
        }
        _categories.value = cats.toList()

        // Core
        val coreUserJson = storage.getString("iitc_core_user", "")
        val coreChannelJson = storage.getString("${ch}_iitc_core", "")
        val coreObj = when {
            coreUserJson.isNotBlank() -> try {
                json.decodeFromString<Plugin>(coreUserJson).copy(override = true)
            } catch (e: Exception) { null }
            coreChannelJson.isNotBlank() -> try {
                json.decodeFromString<Plugin>(coreChannelJson)
            } catch (e: Exception) { null }
            else -> null
        }
        _core.value = coreObj
    }

    suspend fun checkUpdates(force: Boolean = false) = withContext(Dispatchers.Default) {
        if (_isCheckingUpdates.value) return@withContext
        _isCheckingUpdates.value = true
        val ch = _currentChannel.value
        val host = getChannelHost(ch)

        try {
            // 1. Fetch IITC Core
            val coreUrl = "$host/total-conversion-build.user.js"
            try {
                val coreResponse = httpClient.get(coreUrl)
                if (coreResponse.status.value in 200..299) {
                    val coreText = coreResponse.bodyAsText()
                    val parsedCore = PluginParser.parseMeta(coreText)
                    if (parsedCore != null) {
                        saveCore(ch, parsedCore.copy(code = coreText))
                    }
                }
            } catch (e: Exception) {
                println("Failed to fetch core update: ${e.message}")
            }

            // 2. Fetch plugins.json if available
            try {
                val catalogUrl = "$host/plugins.json"
                val catalogResponse = httpClient.get(catalogUrl)
                if (catalogResponse.status.value in 200..299) {
                    val catalogText = catalogResponse.bodyAsText()
                    storage.setString("${ch.key}_plugins_catalog", catalogText)
                }
            } catch (e: Exception) {
                // Ignore 404
            }

            val now = ClockHelper.nowMillis()
            _lastCheckTime.value = now
            storage.setLong("last_check_update", now)
        } catch (e: Exception) {
            println("Update check error: ${e.message}")
        } finally {
            _isCheckingUpdates.value = false
            refreshPluginsView()
        }
    }

    private fun saveCore(channel: Channel, core: Plugin) {
        val jsonStr = json.encodeToString(core)
        storage.setString("${channel.key}_iitc_core", jsonStr)
    }

    fun managePlugin(uid: String, action: String) {
        val stateJson = storage.getString("plugins_state", "{}")
        val stateMap = try {
            json.parseToJsonElement(stateJson).jsonObject.toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }

        when (action) {
            "on", "off" -> {
                stateMap[uid] = JsonPrimitive(action)
                val newJson = JsonObject(stateMap).toString()
                storage.setString("plugins_state", newJson)

                if (action == "on") {
                    val p = _plugins.value[uid]
                    if (p != null && p.code == null) {
                        scope.launch {
                            try {
                                val ch = _currentChannel.value
                                val host = getChannelHost(ch)
                                val scriptUrl = p.downloadURL?.ifBlank { null }
                                    ?: "$host/plugins/${p.filename.ifBlank { "${p.name.replace(" ", "_")}.user.js" }}"
                                val resp = httpClient.get(scriptUrl)
                                if (resp.status.value in 200..299) {
                                    val code = resp.bodyAsText()
                                    cacheLocalPluginCode(ch.key, p.uid, code)
                                    refreshPluginsView()
                                }
                            } catch (e: Exception) {
                                println("Prefetch plugin error: ${e.message}")
                            }
                        }
                    }
                }
            }
            "delete" -> {
                // Delete user plugin or override
                val userPluginsJson = storage.getString("plugins_user", "{}")
                try {
                    val userObj = json.parseToJsonElement(userPluginsJson).jsonObject.toMutableMap()
                    if (userObj.containsKey(uid)) {
                        userObj.remove(uid)
                        storage.setString("plugins_user", JsonObject(userObj).toString())
                    }
                } catch (e: Exception) {
                    // Ignore
                }
                stateMap.remove(uid)
                storage.setString("plugins_state", JsonObject(stateMap).toString())
            }
        }
        refreshPluginsView()
    }

    fun addUserScript(code: String): Plugin? {
        val parsed = PluginParser.parseMeta(code, isUser = true) ?: return null
        val userPluginsJson = storage.getString("plugins_user", "{}")
        val userObj = try {
            json.parseToJsonElement(userPluginsJson).jsonObject.toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }

        userObj[parsed.uid] = json.encodeToJsonElement(parsed)
        storage.setString("plugins_user", JsonObject(userObj).toString())

        // Enable it
        managePlugin(parsed.uid, "on")
        refreshPluginsView()
        return parsed
    }

    suspend fun downloadAndInstallPlugin(url: String): Plugin? = withContext(Dispatchers.Default) {
        try {
            val fullUrl = if (url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)) {
                url
            } else {
                "${getChannelHost(_currentChannel.value)}/plugins/$url"
            }
            val response = httpClient.get(fullUrl)
            if (response.status.value in 200..299) {
                val code = response.bodyAsText()
                return@withContext addUserScript(code)
            }
        } catch (e: Exception) {
            println("Download plugin error: ${e.message}")
        }
        null
    }

    suspend fun getEnabledPluginScripts(): List<String> = withContext(Dispatchers.Default) {
        val scripts = mutableListOf<String>()

        // 1. GM API script
        scripts.add(BridgeScripts.getGmApiCode())

        // 2. Core script
        var activeCore = _core.value
        if (activeCore?.code.isNullOrBlank()) {
            try {
                val coreCode = Res.readBytes("files/total-conversion-build.user.js").decodeToString()
                val parsed = PluginParser.parseMeta(coreCode)
                if (parsed != null) {
                    val fullCore = parsed.copy(code = coreCode)
                    saveCore(_currentChannel.value, fullCore)
                    _core.value = fullCore
                    activeCore = fullCore
                }
            } catch (e: Exception) {
                println("Failed to read fallback core: ${e.message}")
            }
        }

        if (activeCore?.code != null) {
            scripts.add(activeCore.code)
        }

        // 3. Enabled plugins
        val ch = _currentChannel.value
        val host = getChannelHost(ch)
        val enabled = _plugins.value.values.filter { it.isEnabled && !it.isCore }

        for (plugin in enabled) {
            var code = plugin.code
            // If code not cached locally yet, download it
            if (code == null) {
                try {
                    val scriptUrl = plugin.downloadURL?.ifBlank { null }
                        ?: "$host/plugins/${plugin.filename.ifBlank { "${plugin.name.replace(" ", "_")}.user.js" }}"
                    val resp = httpClient.get(scriptUrl)
                    if (resp.status.value in 200..299) {
                        code = resp.bodyAsText()
                        // Cache it in local
                        cacheLocalPluginCode(ch.key, plugin.uid, code)
                    }
                } catch (e: Exception) {
                    println("Failed to fetch plugin script for ${plugin.name}: ${e.message}")
                }
            }

            if (code != null) {
                val wrapped = BridgeScripts.wrapPluginCode(plugin.copy(code = code))
                scripts.add(wrapped)
            }
        }

        scripts
    }

    private fun cacheLocalPluginCode(channel: String, uid: String, code: String) {
        val key = "${channel}_plugins_local"
        val localJson = storage.getString(key, "{}")
        val map = try {
            json.parseToJsonElement(localJson).jsonObject.toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }
        map[uid] = JsonPrimitive(code)
        storage.setString(key, JsonObject(map).toString())
    }

    fun handleGmBridgeRequest(data: String): String? {
        try {
            val elem = json.parseToJsonElement(data).jsonObject
            val taskType = elem["task_type"]?.jsonPrimitive?.content ?: return null
            when (taskType) {
                "setValue" -> {
                    val key = elem["key"]?.jsonPrimitive?.content ?: return null
                    val value = elem["value"]?.toString() ?: return null
                    storage.setString("gm_$key", value)
                }
                "delValue" -> {
                    val key = elem["key"]?.jsonPrimitive?.content ?: return null
                    storage.remove("gm_$key")
                }
            }
        } catch (e: Exception) {
            println("GM bridge request error: ${e.message}")
        }
        return null
    }
}

object ClockHelper {
    fun nowMillis(): Long {
        return io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils.currentTimeMillis()
    }
}
