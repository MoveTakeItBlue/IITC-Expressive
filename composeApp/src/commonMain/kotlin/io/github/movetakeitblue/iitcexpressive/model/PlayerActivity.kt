package io.github.movetakeitblue.iitcexpressive.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerActivityPoint(
    val lat: Double,
    val lng: Double,
    val portalName: String,
    val time: Long
)

@Serializable
data class PlayerTrail(
    val sender: String,
    val team: String,
    val points: List<PlayerActivityPoint>,
    val lastSeenTime: Long,
    val latestLat: Double,
    val latestLng: Double,
    val latestPortalName: String
)
