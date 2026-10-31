package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.farmsfind.FarmsFindSheet

class FarmsFindPlugin : IitcNativePlugin {
    override val id: String = "farms-find"
    override val name: String = "Find Farms"
    override val description: String = "Discover high-level portal clusters (farm hotspots) by level, count, and radius in view."
    override val icon: ImageVector = Icons.Default.Agriculture
    override val category: String = "Info"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        FarmsFindSheet(host = host, onDismiss = onDismiss)
    }
}
