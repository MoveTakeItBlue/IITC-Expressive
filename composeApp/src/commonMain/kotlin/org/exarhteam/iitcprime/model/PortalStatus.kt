package org.exarhteam.iitcprime.model

import kotlinx.serialization.Serializable
import androidx.compose.ui.graphics.Color
import org.exarhteam.iitcprime.theme.*

@Serializable
data class Resonator(
    val level: Int = 0,
    val levelColor: String = "#808080",
    val healthPct: Int = 0,
    val energy: Int = 0
)

@Serializable
data class PortalStatus(
    val guid: String? = null,
    val team: String = "N",
    val level: Int = 0,
    val isNeutral: Boolean = true,
    val title: String = "No portal selected",
    val health: Int = 0,
    val resonators: List<Resonator>? = null,
    val levelColor: String? = null,
    val isLoading: Boolean = false
) {
    val teamType: PortalTeam
        get() = when (team.uppercase()) {
            "E", "ENLIGHTENED" -> PortalTeam.ENLIGHTENED
            "R", "RESISTANCE" -> PortalTeam.RESISTANCE
            "M", "MACHINA" -> PortalTeam.MACHINA
            else -> PortalTeam.NEUTRAL
        }

    val teamColor: Color
        get() = when (teamType) {
            PortalTeam.ENLIGHTENED -> TeamEnlightened
            PortalTeam.RESISTANCE -> TeamResistance
            PortalTeam.MACHINA -> TeamMachina
            PortalTeam.NEUTRAL -> TeamNeutral
        }

    val hasSelected: Boolean
        get() = !guid.isNullOrBlank()
}

enum class PortalTeam {
    ENLIGHTENED,
    RESISTANCE,
    MACHINA,
    NEUTRAL
}
