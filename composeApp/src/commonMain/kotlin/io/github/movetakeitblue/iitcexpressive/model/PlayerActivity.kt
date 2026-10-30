package io.github.movetakeitblue.iitcexpressive.model

import androidx.compose.ui.graphics.Color
import io.github.movetakeitblue.iitcexpressive.theme.*
import kotlinx.serialization.Serializable

@Serializable
data class PlayerActivityPoint(
    val lat: Double,
    val lng: Double,
    val portalName: String,
    val time: Long,
    val formattedTime: String = ""
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
) {
    val teamColor: Color
        get() = when (team.uppercase()) {
            "ENLIGHTENED", "ENL", "2" -> TeamEnlightened
            "RESISTANCE", "RES", "1" -> TeamResistance
            "MACHINA", "MAC", "3" -> TeamMachina
            else -> TeamNeutral
        }

    val teamContainerColor: Color
        get() = when (team.uppercase()) {
            "ENLIGHTENED", "ENL", "2" -> TeamEnlightenedContainer
            "RESISTANCE", "RES", "1" -> TeamResistanceContainer
            "MACHINA", "MAC", "3" -> TeamMachinaContainer
            else -> TeamNeutralContainer
        }

    val onTeamContainerColor: Color
        get() = when (team.uppercase()) {
            "ENLIGHTENED", "ENL", "2" -> OnTeamEnlightenedContainer
            "RESISTANCE", "RES", "1" -> OnTeamResistanceContainer
            "MACHINA", "MAC", "3" -> OnTeamMachinaContainer
            else -> OnTeamNeutralContainer
        }
}

@Serializable
data class UserLocation(
    val lat: Double,
    val lng: Double,
    val accuracy: Double = 0.0,
    val orientation: Double? = null
)
