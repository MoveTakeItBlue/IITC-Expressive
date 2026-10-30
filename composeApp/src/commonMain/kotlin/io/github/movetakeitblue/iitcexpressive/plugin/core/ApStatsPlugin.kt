package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.apstats.ApStatsSheet

class ApStatsPlugin : IitcNativePlugin {
    override val id: String = "ap-stats"
    override val name: String = "Available AP"
    override val description: String = "Calculates available AP to earn in the current map view by destroying, deploying, and linking."
    override val icon: ImageVector = Icons.Default.TrendingUp
    override val category: String = "Info"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        ApStatsSheet(host = host, onDismiss = onDismiss)
    }
}
