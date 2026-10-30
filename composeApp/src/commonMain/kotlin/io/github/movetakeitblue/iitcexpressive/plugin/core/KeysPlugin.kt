package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.keys.KeysSheet

class KeysPlugin : IitcNativePlugin {
    override val id: String = "keys"
    override val name: String = "Keys"
    override val description: String = "Manage portal keys inventory with count adjustments, search, and navigation."
    override val icon: ImageVector = Icons.Default.VpnKey
    override val category: String = "Misc"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        KeysSheet(host = host, onDismiss = onDismiss)
    }
}
