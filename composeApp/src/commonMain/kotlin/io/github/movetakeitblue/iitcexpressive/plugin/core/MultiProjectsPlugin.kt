package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.multiprojects.MultiProjectsSheet

class MultiProjectsPlugin : IitcNativePlugin {
    override val id: String = "multi-projects-extension"
    override val name: String = "Multi Projects"
    override val description: String = "Manage multiple isolated Draw Tools plans and Bookmark collections as named projects."
    override val icon: ImageVector = Icons.Default.FolderSpecial
    override val category: String = "Controls"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        MultiProjectsSheet(host = host, onDismiss = onDismiss)
    }
}
