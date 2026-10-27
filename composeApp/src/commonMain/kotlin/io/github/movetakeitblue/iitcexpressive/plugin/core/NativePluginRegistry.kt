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
}
