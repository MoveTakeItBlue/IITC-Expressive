package io.github.movetakeitblue.iitcexpressive.plugin.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class PortalKeyEntry(
    val guid: String,
    val title: String,
    val count: Int,
    val lat: Double,
    val lng: Double,
    val team: Int = 0 // 0: Neutral, 1: Res, 2: Enl, 3: Mac
)

object KeysSerializer {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Parses IITC standard plugin-keys-data format:
     * { "guid": count, ... } or { "guid": { "count": n, "title": "..." } }
     */
    fun parseJson(jsonString: String): Map<String, Int> {
        if (jsonString.isBlank()) return emptyMap()
        val result = mutableMapOf<String, Int>()
        try {
            val root = json.parseToJsonElement(jsonString).jsonObject
            for ((guid, elem) in root) {
                when (elem) {
                    is JsonPrimitive -> {
                        val count = elem.intOrNull ?: elem.content.toIntOrNull() ?: 0
                        if (count > 0) result[guid] = count
                    }
                    is JsonObject -> {
                        val count = elem["count"]?.jsonPrimitive?.intOrNull ?: 0
                        if (count > 0) result[guid] = count
                    }
                    else -> {}
                }
            }
        } catch (_: Exception) {}
        return result
    }

    fun toJson(map: Map<String, Int>): String {
        val root = buildJsonObject {
            for ((guid, count) in map) {
                if (count > 0) {
                    put(guid, count)
                }
            }
        }
        return root.toString()
    }
}
