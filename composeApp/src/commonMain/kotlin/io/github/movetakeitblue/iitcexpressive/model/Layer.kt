package io.github.movetakeitblue.iitcexpressive.model

import kotlinx.serialization.Serializable

@Serializable
data class MapLayer(
    val layerId: Int,
    val name: String,
    val active: Boolean = false
)

@Serializable
data class Highlighter(
    val name: String
)
