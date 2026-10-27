package io.github.movetakeitblue.iitcexpressive.plugin.api

import io.github.movetakeitblue.iitcexpressive.model.NativePortal
import io.github.movetakeitblue.iitcexpressive.model.PortalStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * Standardized host API provided to IITC native plugins.
 * Encapsulates access to map state, portal entities, persistence, drawing, and bookmarks.
 * Designed to be extracted into :iitc-core-api in Step 3.
 */
interface IitcPluginHost {
    val visiblePortals: StateFlow<List<NativePortal>>
    val selectedPortalGuid: StateFlow<String?>
    val selectedPortal: StateFlow<PortalStatus>
    val drawnShapes: StateFlow<List<DrawnShape>>
    val bookmarkFolders: StateFlow<List<BookmarkFolder>>

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
