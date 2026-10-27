package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.drawtools.DrawToolbar
import io.github.movetakeitblue.iitcexpressive.plugin.ui.drawtools.DrawToolsSheet

class DrawToolsPlugin : IitcNativePlugin {
    override val id: String = "draw-tools"
    override val name: String = "Draw Tools"
    override val description: String = "Draw lines, polygons, and markers onto the map to plan operations."
    override val icon: ImageVector = Icons.Default.Gesture
    override val category: String = "Draw"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        DrawToolsSheet(
            host = host,
            onStartDrawing = { type, color ->
                host.startDrawing(type, color)
            },
            onDismiss = onDismiss
        )
    }

    @Composable
    override fun OverlayContent(host: IitcPluginHost) {
        DrawToolbar(host = host)
    }
}
