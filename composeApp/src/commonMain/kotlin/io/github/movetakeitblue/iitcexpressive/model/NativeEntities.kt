package io.github.movetakeitblue.iitcexpressive.model

import kotlinx.serialization.Serializable

@Serializable
data class NativePortal(
    val guid: String,
    val lat: Double,
    val lng: Double,
    val team: Int, // 0: Neutral, 1: Res, 2: Enl, 3: Mac
    val level: Int,
    val health: Int,
    val resCount: Int = 8
)

@Serializable
data class NativeLink(
    val guid: String,
    val team: Int,
    val oLat: Double,
    val oLng: Double,
    val dLat: Double,
    val dLng: Double
)

@Serializable
data class NativeField(
    val guid: String,
    val team: Int,
    val points: List<List<Double>> // [[lat, lng], [lat, lng], [lat, lng]]
)

@Serializable
data class NativeMapEntities(
    val portals: List<NativePortal> = emptyList(),
    val links: List<NativeLink> = emptyList(),
    val fields: List<NativeField> = emptyList()
)
