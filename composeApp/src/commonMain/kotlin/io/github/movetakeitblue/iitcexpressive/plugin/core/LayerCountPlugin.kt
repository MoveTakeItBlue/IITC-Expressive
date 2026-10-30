package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.layercount.LayerCountSheet

object LayerCountPlugin : IitcNativePlugin {
    override val id: String = "layer-count"
    override val name: String = "Layer Count"
    override val description: String = "Count overlapping control fields and nested layers across the map view"
    override val icon: ImageVector = Icons.Default.Layers
    override val category: String = "Info"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        LayerCountSheet(host = host, onDismiss = onDismiss)
    }
}
