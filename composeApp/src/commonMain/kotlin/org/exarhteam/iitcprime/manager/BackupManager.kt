package org.exarhteam.iitcprime.manager

import kotlinx.serialization.json.*
import okio.Buffer
import okio.ByteString.Companion.toByteString
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.openZip
import org.exarhteam.iitcprime.model.BackupInspection
import org.exarhteam.iitcprime.model.Plugin
import org.exarhteam.iitcprime.platform.PlatformUtils
import org.exarhteam.iitcprime.storage.AppStorage

class BackupManager(
    private val storage: AppStorage,
    private val iitcManager: IITCManager
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; prettyPrint = true }

    fun inspectBackup(bytes: ByteArray): BackupInspection {
        val tempPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "inspect_${PlatformUtils.currentTimeMillis()}.zip"
        return try {
            FileSystem.SYSTEM.write(tempPath) { write(bytes) }
            val zipFs = FileSystem.SYSTEM.openZip(tempPath)
            var hasSettings = false
            var hasData = false
            var hasExternal = false

            for (path in zipFs.list("/".toPath())) {
                val name = path.name
                if (name == "iitc.json") {
                    val content = zipFs.read(path) { readUtf8() }
                    try {
                        val obj = json.parseToJsonElement(content).jsonObject
                        if (obj.containsKey("settings") || obj.containsKey("channel")) hasSettings = true
                        if (obj.containsKey("plugins_data") || obj.size > 2) hasData = true
                    } catch (e: Exception) {
                        // Ignore
                    }
                } else if (name == "shared" || name == "release" || name == "beta" || name == "custom") {
                    hasExternal = true
                }
            }
            BackupInspection(hasSettings = hasSettings, hasData = hasData, hasExternal = hasExternal)
        } catch (e: Exception) {
            BackupInspection(hasSettings = false, hasData = false, hasExternal = false)
        } finally {
            try { FileSystem.SYSTEM.delete(tempPath) } catch (_: Exception) {}
        }
    }

    fun exportBackup(
        exportSettings: Boolean,
        exportData: Boolean,
        exportExternal: Boolean
    ): ByteArray {
        val root = mutableMapOf<String, JsonElement>()

        if (exportSettings) {
            val settingsMap = mutableMapOf<String, JsonElement>()
            settingsMap["channel"] = JsonPrimitive(iitcManager.currentChannel.value.key)
            settingsMap["custom_channel_url"] = JsonPrimitive(iitcManager.customChannelUrl.value)
            val stateJson = storage.getString("plugins_state", "{}")
            try {
                settingsMap["plugins_state"] = json.parseToJsonElement(stateJson)
            } catch (e: Exception) {}
            root["settings"] = JsonObject(settingsMap)
        }

        if (exportData) {
            val dataMap = mutableMapOf<String, JsonElement>()
            for (key in storage.keys()) {
                if (key.startsWith("gm_")) {
                    dataMap[key] = JsonPrimitive(storage.getString(key, ""))
                }
            }
            root["plugins_data"] = JsonObject(dataMap)
        }

        val iitcJsonStr = JsonObject(root).toString()

        // Create Zip with standard zip format
        val buffer = Buffer()
        val entries = mutableListOf<Pair<String, ByteArray>>()
        entries.add("iitc.json" to iitcJsonStr.encodeToByteArray())

        if (exportExternal) {
            val userPlugins = iitcManager.plugins.value.values.filter { it.user || it.override }
            for (plugin in userPlugins) {
                val code = plugin.code
                if (code != null) {
                    val filename = plugin.filename.ifBlank { "${plugin.name.replace(" ", "_")}.user.js" }
                    entries.add("shared/$filename" to code.encodeToByteArray())
                }
            }
        }

        return writeZipBytes(entries)
    }

    fun restoreBackup(
        bytes: ByteArray,
        importSettings: Boolean,
        importData: Boolean,
        importExternal: Boolean
    ): Boolean {
        val tempPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "restore_${PlatformUtils.currentTimeMillis()}.zip"
        return try {
            FileSystem.SYSTEM.write(tempPath) { write(bytes) }
            val zipFs = FileSystem.SYSTEM.openZip(tempPath)

            for (path in zipFs.listRecursively("/".toPath())) {
                val pathStr = path.toString()
                if (pathStr.endsWith("iitc.json")) {
                    val content = zipFs.read(path) { readUtf8() }
                    val obj = json.parseToJsonElement(content).jsonObject

                    if (importSettings && obj.containsKey("settings")) {
                        val sObj = obj["settings"]?.jsonObject
                        val channelStr = sObj?.get("channel")?.jsonPrimitive?.contentOrNull
                        if (channelStr != null) {
                            storage.setString("channel", channelStr)
                        }
                        val stateElem = sObj?.get("plugins_state")
                        if (stateElem != null) {
                            storage.setString("plugins_state", stateElem.toString())
                        }
                    }

                    if (importData && obj.containsKey("plugins_data")) {
                        val dObj = obj["plugins_data"]?.jsonObject
                        if (dObj != null) {
                            for ((k, v) in dObj) {
                                storage.setString(k, v.jsonPrimitive.content)
                            }
                        }
                    }
                } else if (importExternal && pathStr.endsWith(".user.js")) {
                    val code = zipFs.read(path) { readUtf8() }
                    iitcManager.addUserScript(code)
                }
            }
            iitcManager.refreshPluginsView()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try { FileSystem.SYSTEM.delete(tempPath) } catch (_: Exception) {}
        }
    }

    private fun writeZipBytes(entries: List<Pair<String, ByteArray>>): ByteArray {
        // Zip format implementation using standard byte layout
        val out = Buffer()
        val dirEntries = mutableListOf<ByteArray>()
        var offset = 0L

        for ((name, data) in entries) {
            val nameBytes = name.encodeToByteArray()
            val crc = computeCrc32(data)
            val size = data.size

            // Local file header
            val header = Buffer()
            header.writeIntLe(0x04034b50) // Local file header signature
            header.writeShortLe(20) // Version needed to extract
            header.writeShortLe(0) // General purpose bit flag
            header.writeShortLe(0) // Compression method: 0 = stored
            header.writeShortLe(0) // File last mod time
            header.writeShortLe(0) // File last mod date
            header.writeIntLe(crc.toInt()) // CRC-32
            header.writeIntLe(size) // Compressed size
            header.writeIntLe(size) // Uncompressed size
            header.writeShortLe(nameBytes.size) // File name length
            header.writeShortLe(0) // Extra field length
            header.write(nameBytes)
            header.write(data)

            val headerBytes = header.readByteArray()
            out.write(headerBytes)

            // Central directory entry
            val dir = Buffer()
            dir.writeIntLe(0x02014b50) // Central directory header signature
            dir.writeShortLe(20) // Version made by
            dir.writeShortLe(20) // Version needed to extract
            dir.writeShortLe(0) // General purpose bit flag
            dir.writeShortLe(0) // Compression method
            dir.writeShortLe(0) // Last mod time
            dir.writeShortLe(0) // Last mod date
            dir.writeIntLe(crc.toInt()) // CRC-32
            dir.writeIntLe(size) // Compressed size
            dir.writeIntLe(size) // Uncompressed size
            dir.writeShortLe(nameBytes.size) // File name length
            dir.writeShortLe(0) // Extra field length
            dir.writeShortLe(0) // File comment length
            dir.writeShortLe(0) // Disk number start
            dir.writeShortLe(0) // Internal file attributes
            dir.writeIntLe(0) // External file attributes
            dir.writeIntLe(offset.toInt()) // Relative offset of local header
            dir.write(nameBytes)

            dirEntries.add(dir.readByteArray())
            offset += headerBytes.size
        }

        val dirOffset = offset
        var dirSize = 0L
        for (dir in dirEntries) {
            out.write(dir)
            dirSize += dir.size
        }

        // End of central directory record
        val end = Buffer()
        end.writeIntLe(0x06054b50) // End of central dir signature
        end.writeShortLe(0) // Number of this disk
        end.writeShortLe(0) // Number of the disk with start of central dir
        end.writeShortLe(entries.size) // Total entries on this disk
        end.writeShortLe(entries.size) // Total entries
        end.writeIntLe(dirSize.toInt()) // Size of central dir
        end.writeIntLe(dirOffset.toInt()) // Offset of start of central dir
        end.writeShortLe(0) // Zipfile comment length
        out.write(end.readByteArray())

        return out.readByteArray()
    }

    private fun computeCrc32(data: ByteArray): Long {
        var crc = 0xFFFFFFFFL
        for (b in data) {
            val byte = b.toInt() and 0xFF
            var cur = (crc xor byte.toLong()) and 0xFFL
            for (j in 0 until 8) {
                cur = if ((cur and 1L) != 0L) (cur ushr 1) xor 0xEDB88320L else cur ushr 1
            }
            crc = (crc ushr 8) xor cur
        }
        return (crc xor 0xFFFFFFFFL) and 0xFFFFFFFFL
    }
}
