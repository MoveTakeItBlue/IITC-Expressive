package io.github.movetakeitblue.iitcexpressive.plugin.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Standard contract for an IITC native M3E plugin.
 * Can be independently developed, styled, and previewed via Compose Preview.
 * Pre-allocated for extraction into :iitc-core-api and iitc-plugin-template.
 */
interface IitcNativePlugin {
    val id: String
    val name: String
    val description: String
    val icon: ImageVector
    val category: String

    fun onInitialize(host: IitcPluginHost) {}
    fun onDispose() {}

    /**
     * Primary M3E BottomSheet UI content for this plugin.
     */
    @Composable
    fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit)

    /**
     * Optional overlay UI (e.g. floating drawing toolbar) displayed on top of the map.
     */
    @Composable
    fun OverlayContent(host: IitcPluginHost) {}
}
