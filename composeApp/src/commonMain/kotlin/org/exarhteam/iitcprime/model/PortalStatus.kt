package org.exarhteam.iitcprime.model

import kotlinx.serialization.Serializable
import androidx.compose.ui.graphics.Color
import org.exarhteam.iitcprime.theme.*

@Serializable
data class Resonator(
    val level: Int = 0,
    val levelColor: String = "#808080",
    val healthPct: Int = 0,
    val energy: Int = 0,
    val owner: String? = null
) {
    val m3Color: Color
        get() = when (level) {
            1 -> Level1Color
            2 -> Level2Color
            3 -> Level3Color
            4 -> Level4Color
            5 -> Level5Color
            6 -> Level6Color
            7 -> Level7Color
            8 -> Level8Color
            else -> Level0Color
        }
}

@Serializable
data class PortalMod(
    val name: String? = null,
    val rarity: String? = null,
    val owner: String? = null,
    val stats: Map<String, String>? = null
) {
    val rarityColor: Color
        get() = when (rarity?.uppercase()?.replace("_", " ")) {
            "VERY RARE" -> Color(0xFFFF80AB) // M3 Magenta
            "RARE" -> Color(0xFFBA68C8) // M3 Purple
            "COMMON" -> Color(0xFF81C784) // M3 Green
            else -> Color(0xFFA1A1AA)
        }

    val rarityContainerColor: Color
        get() = when (rarity?.uppercase()?.replace("_", " ")) {
            "VERY RARE" -> Color(0x33FF80AB)
            "RARE" -> Color(0x33BA68C8)
            "COMMON" -> Color(0x3381C784)
            else -> Color(0x22A1A1AA)
        }
}

@Serializable
data class PortalDetailResonator(
    val level: Int = 0,
    val owner: String? = null,
    val energy: Int = 0,
    val maxEnergy: Int = 0,
    val distance: Int = 0
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
    val isLoading: Boolean = false,
    val image: String? = null,
    val owner: String? = null,
    val latE6: Long? = null,
    val lngE6: Long? = null,
    val address: String? = null,
    val mods: List<PortalMod>? = null,
    val detailsResonators: List<PortalDetailResonator>? = null,
    val rangeMeters: Long? = null,
    val linksIn: Int = 0,
    val linksOut: Int = 0,
    val fieldsCount: Int = 0,
    val energy: Long = 0,
    val maxEnergy: Long = 0,
    val apGain: Long = 0,
    val historyVisited: Boolean = false,
    val historyCaptured: Boolean = false,
    val historyScoutControlled: Boolean = false
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

    val teamContainerColor: Color
        get() = when (teamType) {
            PortalTeam.ENLIGHTENED -> TeamEnlightenedContainer
            PortalTeam.RESISTANCE -> TeamResistanceContainer
            PortalTeam.MACHINA -> TeamMachinaContainer
            PortalTeam.NEUTRAL -> TeamNeutralContainer
        }

    val onTeamContainerColor: Color
        get() = when (teamType) {
            PortalTeam.ENLIGHTENED -> OnTeamEnlightenedContainer
            PortalTeam.RESISTANCE -> OnTeamResistanceContainer
            PortalTeam.MACHINA -> OnTeamMachinaContainer
            PortalTeam.NEUTRAL -> OnTeamNeutralContainer
        }

    val hasSelected: Boolean
        get() = !guid.isNullOrBlank()

    val lat: Double?
        get() = latE6?.let { it.toDouble() / 1E6 }

    val lng: Double?
        get() = lngE6?.let { it.toDouble() / 1E6 }

    val formattedRange: String
        get() = when {
            rangeMeters == null || rangeMeters <= 0 -> "--"
            rangeMeters >= 1000 -> "${rangeMeters / 1000} km"
            else -> "$rangeMeters m"
        }
}

enum class PortalTeam {
    ENLIGHTENED,
    RESISTANCE,
    MACHINA,
    NEUTRAL
}
