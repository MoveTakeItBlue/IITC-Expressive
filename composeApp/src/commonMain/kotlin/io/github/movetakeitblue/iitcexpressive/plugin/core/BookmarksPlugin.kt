package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.bookmarks.BookmarksSheet

class BookmarksPlugin : IitcNativePlugin {
    override val id: String = "bookmarks"
    override val name: String = "Bookmarks"
    override val description: String = "Save favorite portals and organize into folders with quick navigation."
    override val icon: ImageVector = Icons.Default.Bookmarks
    override val category: String = "Controls"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        BookmarksSheet(host = host, onDismiss = onDismiss)
    }
}
