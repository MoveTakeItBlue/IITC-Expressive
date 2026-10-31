package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.regions.RegionsSheet

class RegionsPlugin : IitcNativePlugin {
    override val id: String = "regions"
    override val name: String = "Scoring Regions"
    override val description: String = "Calculates Ingress S2 Level 6 scoring region codes, boundaries, and regional cycle stats."
    override val icon: ImageVector = Icons.Default.GridOn
    override val category: String = "Info"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        RegionsSheet(host = host, onDismiss = onDismiss)
    }
}
