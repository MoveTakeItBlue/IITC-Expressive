package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.periodicrefresh.PeriodicRefreshSheet

class PeriodicRefreshPlugin : IitcNativePlugin {
    override val id: String = "periodic-refresh"
    override val name: String = "Periodic Refresh"
    override val description: String = "Automatically refreshes map data at selected intervals with safe rate limiting."
    override val icon: ImageVector = Icons.Default.Autorenew
    override val category: String = "Tweaks"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        PeriodicRefreshSheet(host = host, onDismiss = onDismiss)
    }
}
