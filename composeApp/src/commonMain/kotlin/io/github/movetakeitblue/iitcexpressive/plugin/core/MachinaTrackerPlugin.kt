package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.machina.MachinaTrackerSheet

object MachinaTrackerPlugin : IitcNativePlugin {
    override val id: String = "machina-tracker"
    override val name: String = "Machina Tracker"
    override val description: String = "Track and inspect red Machina infected portals, links, and clusters in view"
    override val icon: ImageVector = Icons.Default.Dangerous
    override val category: String = "Layer"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        MachinaTrackerSheet(host = host, onDismiss = onDismiss)
    }
}
