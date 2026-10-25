package io.github.movetakeitblue.iitcexpressive.model

import kotlinx.serialization.Serializable
import io.github.movetakeitblue.iitcexpressive.i18n.L

@Serializable
data class Pane(
    val name: String,
    val label: String,
    val icon: String
) {
    val displayLabel: String
        get() = L(label)

    companion object {
        fun defaultPanes(): List<Pane> = listOf(
            Pane(name = "all", label = "nav.pane.chat_all", icon = "ms-campaign"),
            Pane(name = "faction", label = "nav.pane.chat_faction", icon = "ms-group"),
            Pane(name = "alerts", label = "nav.pane.alerts", icon = "ms-notifications"),
            Pane(name = "info", label = "nav.pane.info", icon = "ms-info"),
            Pane(name = "map", label = "nav.pane.map", icon = "ms-map")
        )
    }
}
