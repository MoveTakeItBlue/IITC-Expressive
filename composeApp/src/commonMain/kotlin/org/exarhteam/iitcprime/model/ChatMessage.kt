package org.exarhteam.iitcprime.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import org.exarhteam.iitcprime.theme.*

@Serializable
data class ChatPortal(
    val name: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

@Serializable
data class ChatMessage(
    val id: String = "",
    val time: Long = 0L,
    val formattedTime: String = "",
    val sender: String = "",
    val team: String = "NEUTRAL",
    val text: String = "",
    val isSecure: Boolean = false,
    val portal: ChatPortal? = null
) {
    val teamColor: Color
        get() = when (team.uppercase()) {
            "ENLIGHTENED" -> TeamEnlightened
            "RESISTANCE" -> TeamResistance
            "MACHINA" -> TeamMachina
            else -> TeamNeutral
        }

    val teamContainerColor: Color
        get() = when (team.uppercase()) {
            "ENLIGHTENED" -> TeamEnlightenedContainer
            "RESISTANCE" -> TeamResistanceContainer
            "MACHINA" -> TeamMachinaContainer
            else -> TeamNeutralContainer
        }

    val onTeamContainerColor: Color
        get() = when (team.uppercase()) {
            "ENLIGHTENED" -> OnTeamEnlightenedContainer
            "RESISTANCE" -> OnTeamResistanceContainer
            "MACHINA" -> OnTeamMachinaContainer
            else -> OnTeamNeutralContainer
        }
}
