package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.portalcounts.PortalCountsSheet

class PortalCountsPlugin : IitcNativePlugin {
    override val id: String = "portal-counts"
    override val name: String = "Portal Counts"
    override val description: String = "Summary count of portals, resonators, links, fields, and energy by faction and level."
    override val icon: ImageVector = Icons.Default.Analytics
    override val category: String = "Info"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        PortalCountsSheet(host = host, onDismiss = onDismiss)
    }
}
