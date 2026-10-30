package io.github.movetakeitblue.iitcexpressive.plugin.core

import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin

object NativePluginRegistry {
    val bookmarksPlugin = BookmarksPlugin()
    val portalListPlugin = PortalListPlugin()
    val drawToolsPlugin = DrawToolsPlugin()
    val scoreboardPlugin = ScoreboardPlugin()
    val apStatsPlugin = ApStatsPlugin()
    val portalCountsPlugin = PortalCountsPlugin()
    val crossLinksPlugin = CrossLinksPlugin()
    val periodicRefreshPlugin = PeriodicRefreshPlugin()
    val keysPlugin = KeysPlugin()
    val uniquesPlugin = UniquesPlugin()
    val machinaTrackerPlugin = MachinaTrackerPlugin
    val layerCountPlugin = LayerCountPlugin

    private val allPlugins = listOf(
        bookmarksPlugin,
        portalListPlugin,
        drawToolsPlugin,
        scoreboardPlugin,
        apStatsPlugin,
        portalCountsPlugin,
        crossLinksPlugin,
        periodicRefreshPlugin,
        keysPlugin,
        uniquesPlugin,
        machinaTrackerPlugin,
        layerCountPlugin
    )

    fun getAll(): List<IitcNativePlugin> = allPlugins

    fun getById(id: String): IitcNativePlugin? {
        val normalized = id.lowercase().replace("-", "").replace("_", "")
        return allPlugins.find {
            it.id.equals(id, ignoreCase = true) ||
            it.id.lowercase().replace("-", "").replace("_", "") == normalized
        }
    }

    /**
     * Additional plugin IDs that have been rewritten natively in MapLibre / Compose,
     * including base maps, highlighters, and native map tweaks.
     * These should be excluded from legacy catalog and script injection.
     */
    private val nativeExcludedIds = setOf(
        // Core Rewritten Plugins & Aliases
        "bookmarks", "drawtools", "draw-tools", "portalslist", "portals-list", "portallist", "portal-list",
        "scoreboard", "scorecycletimes", "score-cycle-times",
        "apstats", "ap-stats",
        "portalcounts", "portal-counts",
        "crosslinks", "cross-links", "donelinks", "done-links",
        "periodicrefresh", "periodic-refresh",
        "keys", "keysonmap", "keys-on-map",
        "uniques",
        "playeractivitytracker", "player-activity-tracker",

        // Base Maps (Native MapLibre Vector/Raster Tiles)
        "basemapbing", "basemap-bing",
        "basemapblank", "basemap-blank",
        "basemapgaode", "basemap-gaode",
        "basemapgooglegray", "basemap-google-gray",
        "basemapkartverket", "basemap-kartverket",
        "basemapopenstreetmap", "basemap-openstreetmap",
        "basemapstamen", "basemap-stamen",
        "basemapyandex", "basemap-yandex",

        // Map Highlighters (Native MapLibre GPU Styling)
        "highlightforgotten", "highlight-forgotten",
        "highlighthideteam", "highlight-hide-team",
        "highlighthighlevel", "highlight-high-level",
        "highlightlevelcolor", "highlight-level-color",
        "highlightmissingresonators", "highlight-missing-resonators",
        "highlightmovedportals", "highlight-moved-portals",
        "highlightneedsrecharge", "highlight-needs-recharge",
        "highlightornaments", "highlight-ornaments",
        "highlightportalhistory", "highlight-portal-history",
        "highlightportalsmylevel", "highlight-portals-my-level",
        "highlightweakness", "highlight-weakness",

        // Map Tweaks & Enhancements (Native MapLibre & Compose Sheets)
        "portalnames", "portal-names",
        "portallevelnumbers", "portal-level-numbers",
        "resoenergypct", "reso-energy-pct",
        "linkedportalsshow", "linked-portals-show",
        "userlocation", "user-location",
        "scalebar", "scale-bar",
        "pancontrol", "pan-control",
        "zoomslider", "zoom-slider",
        "scrollwheelzoomdisable", "scroll-wheel-zoom-disable",
        "fixchinamapoffset", "fix-china-map-offset",

        // Additional Built-in Plugins Handled Natively
        "machina-tracker", "machinatracker", "machina-tools", "machinatools",
        "layer-count", "layercount",
        "distance-to-portal", "distancetoportal",
        "debug-raw-portal-data", "debugrawportaldata",
        "privacy-view", "privacyview",
        "cache-portals-on-map", "cacheportalsonmap",
        "link-show-direction", "linkshowdirection",
        "zaprange", "wayfarer-range", "wayfarerrange",
        "minimap", "hide-portal-levels", "hideportallevels",
        "remove-extra-layers", "removeextralayers",
        "ornament-icons", "ornamenticons",
        "ornament-icons-extended", "ornamenticonsextended"
    )

    /**
     * Returns true if the plugin has been rewritten natively in Compose,
     * so it should be excluded from the legacy userscripts list to avoid user confusion.
     */
    fun isNativePlugin(id: String?): Boolean {
        if (id.isNullOrBlank()) return false
        val normalized = id.lowercase().replace("-", "").replace("_", "")
        if (nativeExcludedIds.contains(id.lowercase()) || nativeExcludedIds.contains(normalized)) {
            return true
        }
        return allPlugins.any {
            it.id.equals(id, ignoreCase = true) ||
            it.id.lowercase().replace("-", "").replace("_", "") == normalized
        }
    }
}
