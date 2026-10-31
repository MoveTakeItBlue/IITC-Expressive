package io.github.movetakeitblue.iitcexpressive.plugin.api

import io.github.movetakeitblue.iitcexpressive.model.ChatMessage
import io.github.movetakeitblue.iitcexpressive.model.NativeField
import io.github.movetakeitblue.iitcexpressive.model.NativeLink
import io.github.movetakeitblue.iitcexpressive.model.NativeMapEntities
import io.github.movetakeitblue.iitcexpressive.model.NativePortal
import io.github.movetakeitblue.iitcexpressive.model.PortalStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * Standardized host API provided to IITC native plugins.
 * Encapsulates access to map state, portal entities, persistence, drawing, bookmarks,
 * regional scoreboard, key inventory, chat messages, and periodic refresh.
 * Designed to be extracted into :iitc-core-api in Step 3.
 */
interface IitcPluginHost {
    val visiblePortals: StateFlow<List<NativePortal>>
    val visibleEntities: StateFlow<NativeMapEntities>
    val visibleLinks: StateFlow<List<NativeLink>>
    val visibleFields: StateFlow<List<NativeField>>
    val selectedPortalGuid: StateFlow<String?>
    val selectedPortal: StateFlow<PortalStatus>
    val drawnShapes: StateFlow<List<DrawnShape>>
    val bookmarkFolders: StateFlow<List<BookmarkFolder>>
    val chatMessages: StateFlow<Map<String, List<ChatMessage>>>

    // Scoreboard & Cycle
    val regionScoreData: StateFlow<RegionScoreData?>
    val isScoreboardLoading: StateFlow<Boolean>
    fun requestRegionScore()

    // Periodic Refresh
    val periodicRefreshIntervalSeconds: StateFlow<Int>
    val timeToNextAutoRefreshSeconds: StateFlow<Int>
    fun setPeriodicRefreshInterval(seconds: Int)
    fun triggerMapRefresh()

    // Key Inventory
    val portalKeys: StateFlow<Map<String, Int>>
    fun getPortalKeyCount(guid: String): Int
    fun setPortalKeyCount(guid: String, count: Int)

    // Drawing state
    val isDrawingModeActive: StateFlow<Boolean>
    val currentDrawingType: StateFlow<String> // "polyline", "polygon", "marker"
    val currentDrawingColor: StateFlow<String>
    val currentDrawingPoints: StateFlow<List<ShapeLatLng>>

    // Map operations
    fun flyTo(lat: Double, lng: Double, zoom: Double? = null)
    fun selectPortal(guid: String)
    fun deselectPortal()

    // Data persistence & mutations
    fun saveDrawnShapes(shapes: List<DrawnShape>)
    fun saveBookmarkFolders(folders: List<BookmarkFolder>)
    fun togglePortalBookmark(portal: NativePortal): Boolean
    fun isPortalBookmarked(guid: String): Boolean

    // Drawing operations
    fun startDrawing(type: String = "polyline", color: String = "#a24ac3")
    fun addDrawingPoint(lat: Double, lng: Double)
    fun undoDrawingPoint()
    fun finishDrawing()
    fun cancelDrawing()
    fun setDrawingColor(color: String)
    fun setDrawingType(type: String)

    // Platform utilities
    fun showToast(message: String)
    fun executeJs(script: String)
}
