package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.crosslinks.CrossLinksSheet

class CrossLinksPlugin : IitcNativePlugin {
    override val id: String = "cross-links"
    override val name: String = "Cross Links"
    override val description: String = "Detects active map links intersecting with drawn plans from Draw Tools."
    override val icon: ImageVector = Icons.Default.AltRoute
    override val category: String = "Draw"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        CrossLinksSheet(host = host, onDismiss = onDismiss)
    }
}
