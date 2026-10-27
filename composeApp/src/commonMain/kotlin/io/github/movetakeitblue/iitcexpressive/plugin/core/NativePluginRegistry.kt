package io.github.movetakeitblue.iitcexpressive.plugin.core

import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin

object NativePluginRegistry {
    val bookmarksPlugin = BookmarksPlugin()
    val portalListPlugin = PortalListPlugin()
    val drawToolsPlugin = DrawToolsPlugin()

    private val allPlugins = listOf(
        bookmarksPlugin,
        portalListPlugin,
        drawToolsPlugin
    )

    fun getAll(): List<IitcNativePlugin> = allPlugins

    fun getById(id: String): IitcNativePlugin? {
        return allPlugins.find { it.id.equals(id, ignoreCase = true) }
    }

    /**
     * Returns true if the plugin has been rewritten natively in Compose,
     * so it should be excluded from the legacy userscripts list to avoid user confusion.
     */
    fun isNativePlugin(id: String?): Boolean {
        if (id.isNullOrBlank()) return false
        val normalized = id.lowercase().replace("-", "").replace("_", "")
        return allPlugins.any {
            it.id.equals(id, ignoreCase = true) ||
            it.id.lowercase().replace("-", "").replace("_", "") == normalized
        }
    }
}
