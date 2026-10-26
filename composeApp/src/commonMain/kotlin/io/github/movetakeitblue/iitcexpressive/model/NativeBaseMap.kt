package io.github.movetakeitblue.iitcexpressive.model

import kotlinx.serialization.Serializable

@Serializable
enum class NativeBaseMap(
    val id: String,
    val title: String,
    val subtitle: String,
    val isDark: Boolean
) {
    DARK("dark", "Dark Canvas", "Esri World Dark (Free)", true),
    LIGHT("light", "Light Canvas", "Esri World Light (Free)", false),
    SATELLITE("satellite", "Satellite", "Esri World Imagery (Free)", true),
    COLOR("color", "Streets Color", "OpenStreetMap (Free)", false);

    companion object {
        fun fromId(id: String): NativeBaseMap = entries.find { it.id == id } ?: DARK
    }
}
