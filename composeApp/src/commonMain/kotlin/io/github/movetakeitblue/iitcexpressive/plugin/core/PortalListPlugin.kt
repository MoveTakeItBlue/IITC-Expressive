package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.portallist.PortalListSheet

class PortalListPlugin : IitcNativePlugin {
    override val id: String = "portals-list"
    override val name: String = "Portals List"
    override val description: String = "Display sortable and filterable list of all visible portals with stats."
    override val icon: ImageVector = Icons.Default.FormatListBulleted
    override val category: String = "Info"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        PortalListSheet(host = host, onDismiss = onDismiss)
    }
}
