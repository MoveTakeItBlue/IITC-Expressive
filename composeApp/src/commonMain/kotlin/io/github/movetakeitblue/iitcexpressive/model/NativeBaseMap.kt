package io.github.movetakeitblue.iitcexpressive.model

import kotlinx.serialization.Serializable

@Serializable
enum class NativeBaseMap(
    val id: String,
    val title: String,
    val subtitle: String,
    val isDark: Boolean,
    val isGcj02: Boolean = false
) {
    DARK("dark", "Dark Canvas", "Esri World Dark (Free)", true, false),
    LIGHT("light", "Light Canvas", "Esri World Light (Free)", false, false),
    SATELLITE("satellite", "Satellite", "Esri World Imagery (Free)", true, false),
    COLOR("color", "Streets Color", "OpenStreetMap (Free)", false, false),

    // Google Maps Layers (Global High-Resolution WGS-84)
    GOOGLE_ROAD("google_road", "Google Maps", "Roadmap (Global)", false, false),
    GOOGLE_HYBRID("google_hybrid", "Google Satellite", "Hybrid Imagery (Global)", true, false),
    GOOGLE_TERRAIN("google_terrain", "Google Terrain", "Terrain with Relief (Global)", false, false),

    // AutoNavi / Gaode Layers (High-Performance China & Global, GCJ-02 calibrated)
    GAODE_VECTOR("gaode_vector", "AutoNavi (高德地图)", "Vector Streets (GCJ-02)", false, true),
    GAODE_SATELLITE("gaode_satellite", "AutoNavi Satellite (高德卫星)", "Satellite & Roads (GCJ-02)", true, true);

    companion object {
        fun fromId(id: String): NativeBaseMap = entries.find { it.id == id } ?: DARK
    }
}
