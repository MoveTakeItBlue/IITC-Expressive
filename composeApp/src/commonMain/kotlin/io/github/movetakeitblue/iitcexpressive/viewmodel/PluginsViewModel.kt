package io.github.movetakeitblue.iitcexpressive.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import io.github.movetakeitblue.iitcexpressive.manager.IITCManager
import io.github.movetakeitblue.iitcexpressive.model.Plugin
import io.github.movetakeitblue.iitcexpressive.plugin.core.NativePluginRegistry

class PluginsViewModel(
    val manager: IITCManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeCategory = MutableStateFlow("All")
    val activeCategory: StateFlow<String> = _activeCategory.asStateFlow()

    val categories: StateFlow<List<String>> = manager.categories

    val filteredPlugins: StateFlow<List<Plugin>> = combine(
        manager.plugins,
        _searchQuery,
        _activeCategory
    ) { pluginsMap, query, category ->
        val list = pluginsMap.values.toList()
        list.filter { plugin ->
            if (NativePluginRegistry.isNativePlugin(plugin.uid)) return@filter false
            val matchCategory = if (category == "All") true else plugin.category.equals(category, ignoreCase = true)
            val matchQuery = if (query.isBlank()) true else {
                plugin.name.contains(query, ignoreCase = true) ||
                plugin.description.contains(query, ignoreCase = true) ||
                plugin.author.contains(query, ignoreCase = true)
            }
            matchCategory && matchQuery
        }.sortedWith(
            compareByDescending<Plugin> { if (category == "All") it.isEnabled else false }
                .thenBy { it.name.lowercase() }
        )
    }.stateIn(scope, SharingStarted.Lazily, emptyList())

    val corePlugin: StateFlow<Plugin?> = combine(
        manager.core,
        _searchQuery,
        _activeCategory
    ) { core, query, category ->
        if (category == "All" && query.isBlank()) core else null
    }.stateIn(scope, SharingStarted.Lazily, manager.core.value)

    private val _selectedPluginForInfo = MutableStateFlow<Plugin?>(null)
    val selectedPluginForInfo: StateFlow<Plugin?> = _selectedPluginForInfo.asStateFlow()

    private val _pendingPlugin = MutableStateFlow<Plugin?>(null)
    val pendingPlugin: StateFlow<Plugin?> = _pendingPlugin.asStateFlow()

    private val _showAddPluginSheet = MutableStateFlow(false)
    val showAddPluginSheet: StateFlow<Boolean> = _showAddPluginSheet.asStateFlow()

    fun showPluginInfo(plugin: Plugin) {
        _selectedPluginForInfo.value = plugin
    }

    fun dismissPluginInfo() {
        _selectedPluginForInfo.value = null
        _pendingPlugin.value = null
    }

    fun showAddPlugin() {
        _showAddPluginSheet.value = true
    }

    fun dismissAddPlugin() {
        _showAddPluginSheet.value = false
    }

    fun loadPluginFromUrl(url: String) {
        _showAddPluginSheet.value = false
        scope.launch {
            val plugin = manager.downloadAndInstallPlugin(url)
            if (plugin != null) {
                _pendingPlugin.value = plugin
            }
        }
    }

    fun loadPluginFromFile() {
        _showAddPluginSheet.value = false
        val bytes = io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils.pickFile() ?: return
        val code = bytes.decodeToString()
        val plugin = manager.addUserScript(code)
        if (plugin != null) {
            _pendingPlugin.value = plugin
        }
    }

    fun installPendingPlugin() {
        _pendingPlugin.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setActiveCategory(category: String) {
        _activeCategory.value = category
    }

    fun togglePlugin(plugin: Plugin) {
        val nextAction = if (plugin.isEnabled) "off" else "on"
        manager.managePlugin(plugin.uid, nextAction)
    }

    fun deletePlugin(plugin: Plugin) {
        manager.managePlugin(plugin.uid, "delete")
    }

    fun installPlugin(code: String): Plugin? {
        return manager.addUserScript(code)
    }

    fun downloadAndInstall(url: String, onResult: (Boolean) -> Unit) {
        scope.launch {
            val result = manager.downloadAndInstallPlugin(url)
            onResult(result != null)
        }
    }
}
