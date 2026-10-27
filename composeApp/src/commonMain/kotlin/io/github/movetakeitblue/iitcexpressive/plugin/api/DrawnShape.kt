package io.github.movetakeitblue.iitcexpressive.plugin.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class ShapeLatLng(
    val lat: Double,
    val lng: Double
)

@Serializable
sealed class DrawnShape {
    abstract val id: String
    abstract val color: String

    @Serializable
    data class Polyline(
        override val id: String,
        override val color: String = "#a24ac3",
        val latLngs: List<ShapeLatLng>
    ) : DrawnShape()

    @Serializable
    data class Polygon(
        override val id: String,
        override val color: String = "#a24ac3",
        val latLngs: List<ShapeLatLng>
    ) : DrawnShape()

    @Serializable
    data class Circle(
        override val id: String,
        override val color: String = "#a24ac3",
        val center: ShapeLatLng,
        val radiusMeters: Double
    ) : DrawnShape()

    @Serializable
    data class Marker(
        override val id: String,
        override val color: String = "#a24ac3",
        val latLng: ShapeLatLng,
        val label: String = ""
    ) : DrawnShape()

    companion object {
        private val json = Json { ignoreUnknownKeys = true; isLenient = true }

        /**
         * Parses IITC standard draw-tools JSON array (from localStorage['plugin-draw-tools-layer'])
         */
        fun parseIitcJson(jsonString: String): List<DrawnShape> {
            if (jsonString.isBlank()) return emptyList()
            return try {
                val element = json.parseToJsonElement(jsonString)
                if (element !is JsonArray) return emptyList()

                element.mapIndexedNotNull { index, item ->
                    if (item !is JsonObject) return@mapIndexedNotNull null
                    val type = item["type"]?.jsonPrimitive?.contentOrNull ?: return@mapIndexedNotNull null
                    val color = item["color"]?.jsonPrimitive?.contentOrNull ?: "#a24ac3"
                    val id = item["id"]?.jsonPrimitive?.contentOrNull ?: "shape_$index"

                    when (type.lowercase()) {
                        "polyline", "line" -> {
                            val pts = parseLatLngs(item["latLngs"])
                            if (pts.size >= 2) Polyline(id = id, color = color, latLngs = pts) else null
                        }
                        "polygon" -> {
                            val pts = parseLatLngs(item["latLngs"])
                            if (pts.size >= 3) Polygon(id = id, color = color, latLngs = pts) else null
                        }
                        "circle" -> {
                            val center = parseSingleLatLng(item["latLng"])
                            val radius = item["radius"]?.jsonPrimitive?.doubleOrNull ?: 500.0
                            if (center != null) Circle(id = id, color = color, center = center, radiusMeters = radius) else null
                        }
                        "marker" -> {
                            val pt = parseSingleLatLng(item["latLng"])
                            val label = item["label"]?.jsonPrimitive?.contentOrNull ?: ""
                            if (pt != null) Marker(id = id, color = color, latLng = pt, label = label) else null
                        }
                        else -> null
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

        private fun parseLatLngs(element: JsonElement?): List<ShapeLatLng> {
            if (element !is JsonArray) return emptyList()
            return element.mapNotNull { parseSingleLatLng(it) }
        }

        private fun parseSingleLatLng(element: JsonElement?): ShapeLatLng? {
            return when (element) {
                is JsonObject -> {
                    val lat = element["lat"]?.jsonPrimitive?.doubleOrNull ?: return null
                    val lng = element["lng"]?.jsonPrimitive?.doubleOrNull ?: return null
                    ShapeLatLng(lat, lng)
                }
                is JsonArray -> {
                    if (element.size >= 2) {
                        val lat = element[0].jsonPrimitive.doubleOrNull ?: return null
                        val lng = element[1].jsonPrimitive.doubleOrNull ?: return null
                        ShapeLatLng(lat, lng)
                    } else null
                }
                else -> null
            }
        }

        /**
         * Serializes shapes back to IITC standard draw-tools JSON format.
         */
        fun toIitcJson(shapes: List<DrawnShape>): String {
            val array = buildJsonArray {
                shapes.forEach { shape ->
                    addJsonObject {
                        put("color", shape.color)
                        put("id", shape.id)
                        when (shape) {
                            is Polyline -> {
                                put("type", "polyline")
                                putJsonArray("latLngs") {
                                    shape.latLngs.forEach { pt ->
                                        addJsonObject {
                                            put("lat", pt.lat)
                                            put("lng", pt.lng)
                                        }
                                    }
                                }
                            }
                            is Polygon -> {
                                put("type", "polygon")
                                putJsonArray("latLngs") {
                                    shape.latLngs.forEach { pt ->
                                        addJsonObject {
                                            put("lat", pt.lat)
                                            put("lng", pt.lng)
                                        }
                                    }
                                }
                            }
                            is Circle -> {
                                put("type", "circle")
                                putJsonObject("latLng") {
                                    put("lat", shape.center.lat)
                                    put("lng", shape.center.lng)
                                }
                                put("radius", shape.radiusMeters)
                            }
                            is Marker -> {
                                put("type", "marker")
                                putJsonObject("latLng") {
                                    put("lat", shape.latLng.lat)
                                    put("lng", shape.latLng.lng)
                                }
                                if (shape.label.isNotBlank()) {
                                    put("label", shape.label)
                                }
                            }
                        }
                    }
                }
            }
            return array.toString()
        }
    }
}
