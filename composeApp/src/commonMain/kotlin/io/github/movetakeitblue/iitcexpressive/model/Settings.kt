package io.github.movetakeitblue.iitcexpressive.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val desktopMode: Boolean = false,
    val zoomControl: Boolean = false,
    val fakeUserAgent: Boolean = false,
    val persistentZoom: Boolean = false,
    val showLocation: Boolean = false,
    val fullscreenMap: Boolean = false,
    val mapTextColor: String = "auto",
    val updateInterval: Long = 86400,
    val externalUpdateInterval: Long = 86400,
    val customChannelUrl: String = ""
)
