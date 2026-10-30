package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.uniques.UniquesSheet

class UniquesPlugin : IitcNativePlugin {
    override val id: String = "uniques"
    override val name: String = "Uniques"
    override val description: String = "Tracks visited, captured, and scout controlled portals from mission history."
    override val icon: ImageVector = Icons.Default.Explore
    override val category: String = "Misc"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        UniquesSheet(host = host, onDismiss = onDismiss)
    }
}
