package io.github.movetakeitblue.iitcexpressive.model

import kotlinx.serialization.Serializable

@Serializable
data class PortalLevelsInfo(
    val hasPortals: Boolean = false,
    val minLinkLength: Long = 0,
    val formattedLength: String = "0m"
)

@Serializable
data class MapStatusDetails(
    val short: String? = null,
    val long: String? = null,
    val progress: Float = 1f,
    val progressPercent: Int? = 100
)

@Serializable
data class MapRequestsInfo(
    val active: Int = 0,
    val failed: Int = 0,
    val hasActive: Boolean = false,
    val hasFailed: Boolean = false
)

@Serializable
data class MapStatus(
    val portalLevels: PortalLevelsInfo = PortalLevelsInfo(),
    val mapStatus: MapStatusDetails = MapStatusDetails(),
    val requests: MapRequestsInfo = MapRequestsInfo()
)
