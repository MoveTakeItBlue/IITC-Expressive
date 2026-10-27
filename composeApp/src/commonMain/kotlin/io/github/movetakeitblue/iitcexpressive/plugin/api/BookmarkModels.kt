package io.github.movetakeitblue.iitcexpressive.plugin.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class PortalBookmark(
    val guid: String,
    val label: String,
    val lat: Double,
    val lng: Double,
    val team: Int = 0, // 0: Neutral, 1: Res, 2: Enl, 3: Mac
    val level: Int = 1
)

@Serializable
data class BookmarkFolder(
    val id: String,
    val name: String,
    val bookmarks: List<PortalBookmark> = emptyList()
)

object BookmarkSerializer {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Parses IITC standard plugin-bookmarks-portals-data JSON.
     */
    fun parseIitcJson(jsonString: String): List<BookmarkFolder> {
        if (jsonString.isBlank()) return defaultFolders()
        return try {
            val root = json.parseToJsonElement(jsonString).jsonObject
            val folders = mutableListOf<BookmarkFolder>()

            // Format A: { "portals": { "folderId": { "bkmrkId": { "guid": "...", "latlng": "lat,lng", "label": "..." } } } }
            val portalsObj = root["portals"]?.jsonObject
            if (portalsObj != null && portalsObj.isNotEmpty()) {
                portalsObj.forEach { (folderId, bkmrksElement) ->
                    val bkmrkList = mutableListOf<PortalBookmark>()
                    if (bkmrksElement is JsonObject) {
                        bkmrksElement.forEach { (_, bkmrkEl) ->
                            if (bkmrkEl is JsonObject) {
                                val guid = bkmrkEl["guid"]?.jsonPrimitive?.contentOrNull ?: ""
                                val label = bkmrkEl["label"]?.jsonPrimitive?.contentOrNull ?: "Portal"
                                val latlngStr = bkmrkEl["latlng"]?.jsonPrimitive?.contentOrNull ?: ""
                                var lat = 0.0
                                var lng = 0.0
                                if (latlngStr.contains(",")) {
                                    val parts = latlngStr.split(",")
                                    lat = parts[0].trim().toDoubleOrNull() ?: 0.0
                                    lng = parts[1].trim().toDoubleOrNull() ?: 0.0
                                } else {
                                    lat = bkmrkEl["lat"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                                    lng = bkmrkEl["lng"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                                }
                                val team = bkmrkEl["team"]?.jsonPrimitive?.intOrNull ?: 0
                                val level = bkmrkEl["level"]?.jsonPrimitive?.intOrNull ?: 1
                                if (guid.isNotBlank() || (lat != 0.0 && lng != 0.0)) {
                                    bkmrkList.add(PortalBookmark(guid, label, lat, lng, team, level))
                                }
                            }
                        }
                    }
                    val folderName = if (folderId == "idOthers") "General" else folderId
                    folders.add(BookmarkFolder(id = folderId, name = folderName, bookmarks = bkmrkList))
                }
            }

            // Format B: { "folders": [ { "id": "...", "name": "...", "bookmarks": [...] } ] }
            val foldersArray = root["folders"]?.jsonArray
            if (folders.isEmpty() && foldersArray != null) {
                foldersArray.forEach { folderEl ->
                    if (folderEl is JsonObject) {
                        val id = folderEl["id"]?.jsonPrimitive?.contentOrNull ?: "folder_${folders.size}"
                        val name = folderEl["name"]?.jsonPrimitive?.contentOrNull ?: id
                        val bkmrks = folderEl["bookmarks"]?.jsonArray?.mapNotNull { bEl ->
                            if (bEl is JsonObject) {
                                val guid = bEl["guid"]?.jsonPrimitive?.contentOrNull ?: ""
                                val label = bEl["label"]?.jsonPrimitive?.contentOrNull ?: ""
                                val lat = bEl["lat"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                                val lng = bEl["lng"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                                val team = bEl["team"]?.jsonPrimitive?.intOrNull ?: 0
                                val level = bEl["level"]?.jsonPrimitive?.intOrNull ?: 1
                                PortalBookmark(guid, label, lat, lng, team, level)
                            } else null
                        } ?: emptyList()
                        folders.add(BookmarkFolder(id, name, bkmrks))
                    }
                }
            }

            if (folders.isEmpty()) defaultFolders() else folders
        } catch (e: Exception) {
            defaultFolders()
        }
    }

    /**
     * Serializes bookmark folders into IITC-compatible plugin-bookmarks JSON.
     */
    fun toIitcJson(folders: List<BookmarkFolder>): String {
        val root = buildJsonObject {
            putJsonObject("portals") {
                folders.forEach { folder ->
                    val folderKey = if (folder.id == "default" || folder.id == "idOthers") "idOthers" else folder.id
                    putJsonObject(folderKey) {
                        folder.bookmarks.forEachIndexed { idx, bkmrk ->
                            val bkmrkKey = if (bkmrk.guid.isNotBlank()) "bkmrk_${bkmrk.guid}" else "bkmrk_$idx"
                            putJsonObject(bkmrkKey) {
                                put("guid", bkmrk.guid)
                                put("label", bkmrk.label)
                                put("latlng", "${bkmrk.lat},${bkmrk.lng}")
                                put("team", bkmrk.team)
                                put("level", bkmrk.level)
                            }
                        }
                    }
                }
            }
            putJsonObject("maps") {}
            putJsonArray("order") {
                folders.forEach { folder ->
                    add(if (folder.id == "default") "idOthers" else folder.id)
                }
            }
            put("defFolder", if (folders.isNotEmpty()) (if (folders[0].id == "default") "idOthers" else folders[0].id) else "idOthers")
        }
        return root.toString()
    }

    fun defaultFolders(): List<BookmarkFolder> = listOf(
        BookmarkFolder(id = "idOthers", name = "General", bookmarks = emptyList())
    )
}
