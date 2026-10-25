package io.github.movetakeitblue.iitcexpressive.model

import kotlinx.serialization.Serializable

@Serializable
data class Plugin(
    val uid: String,
    val name: String = "",
    val author: String = "",
    val category: String = "Misc",
    val version: String = "",
    val description: String = "",
    val filename: String = "",
    val homepageURL: String? = null,
    val updateURL: String? = null,
    val downloadURL: String? = null,
    val issueTracker: String? = null,
    val status: String = "off",
    val user: Boolean = false,
    val override: Boolean = false,
    val addedAt: Long? = null,
    val statusChangedAt: Long? = null,
    val updatedAt: Long? = null,
    val code: String? = null
) {
    val id: String
        get() = uid

    val isEnabled: Boolean
        get() = status == "on"

    val isUserOrOverride: Boolean
        get() = user || override

    val isCore: Boolean
        get() = uid == "total-conversion-build" || category.equals("Core", ignoreCase = true)
}
