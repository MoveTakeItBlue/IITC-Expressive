package io.github.movetakeitblue.iitcexpressive.plugin.api

import io.github.movetakeitblue.iitcexpressive.model.NativePortal
import io.github.movetakeitblue.iitcexpressive.model.PortalStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Mock implementation of IitcPluginHost for Compose Previews, Unit Tests,
 * and independent plugin template development (Step 3).
 */
class MockIitcPluginHost(
    initialPortals: List<NativePortal> = samplePortals(),
    initialBookmarks: List<BookmarkFolder> = sampleBookmarks(),
    initialShapes: List<DrawnShape> = sampleShapes()
) : IitcPluginHost {

    private val _visiblePortals = MutableStateFlow(initialPortals)
    override val visiblePortals: StateFlow<List<NativePortal>> = _visiblePortals.asStateFlow()

    private val _selectedPortalGuid = MutableStateFlow<String?>("portal_1")
    override val selectedPortalGuid: StateFlow<String?> = _selectedPortalGuid.asStateFlow()

    private val _selectedPortal = MutableStateFlow(
        PortalStatus(
            guid = "portal_1",
            title = "Forbidden City Meridian Gate",
            team = "RES",
            level = 8,
            health = 100,
            resonators = List(8) { io.github.movetakeitblue.iitcexpressive.model.Resonator(level = 8, healthPct = 100) },
            latE6 = (39.914 * 1e6).toLong(),
            lngE6 = (116.397 * 1e6).toLong()
        )
    )
    override val selectedPortal: StateFlow<PortalStatus> = _selectedPortal.asStateFlow()

    private val _drawnShapes = MutableStateFlow(initialShapes)
    override val drawnShapes: StateFlow<List<DrawnShape>> = _drawnShapes.asStateFlow()

    private val _bookmarkFolders = MutableStateFlow(initialBookmarks)
    override val bookmarkFolders: StateFlow<List<BookmarkFolder>> = _bookmarkFolders.asStateFlow()

    private val _isDrawingModeActive = MutableStateFlow(false)
    override val isDrawingModeActive: StateFlow<Boolean> = _isDrawingModeActive.asStateFlow()

    private val _currentDrawingType = MutableStateFlow("polyline")
    override val currentDrawingType: StateFlow<String> = _currentDrawingType.asStateFlow()

    private val _currentDrawingColor = MutableStateFlow("#a24ac3")
    override val currentDrawingColor: StateFlow<String> = _currentDrawingColor.asStateFlow()

    private val _currentDrawingPoints = MutableStateFlow<List<ShapeLatLng>>(emptyList())
    override val currentDrawingPoints: StateFlow<List<ShapeLatLng>> = _currentDrawingPoints.asStateFlow()

    override fun flyTo(lat: Double, lng: Double, zoom: Double?) {}
    override fun selectPortal(guid: String) { _selectedPortalGuid.value = guid }
    override fun deselectPortal() { _selectedPortalGuid.value = null }

    override fun saveDrawnShapes(shapes: List<DrawnShape>) {
        _drawnShapes.value = shapes
    }

    override fun saveBookmarkFolders(folders: List<BookmarkFolder>) {
        _bookmarkFolders.value = folders
    }

    override fun togglePortalBookmark(portal: NativePortal): Boolean {
        val folders = _bookmarkFolders.value.toMutableList()
        if (folders.isEmpty()) {
            folders.add(BookmarkFolder("idOthers", "General", emptyList()))
        }
        val def = folders[0]
        val existing = def.bookmarks.find { it.guid == portal.guid }
        val updated = if (existing != null) {
            folders[0] = def.copy(bookmarks = def.bookmarks.filter { it.guid != portal.guid })
            false
        } else {
            folders[0] = def.copy(
                bookmarks = def.bookmarks + PortalBookmark(
                    guid = portal.guid,
                    label = portal.title.ifBlank { "Portal" },
                    lat = portal.lat,
                    lng = portal.lng,
                    team = portal.team,
                    level = portal.level
                )
            )
            true
        }
        _bookmarkFolders.value = folders
        return updated
    }

    override fun isPortalBookmarked(guid: String): Boolean {
        return _bookmarkFolders.value.any { folder -> folder.bookmarks.any { it.guid == guid } }
    }

    override fun startDrawing(type: String, color: String) {
        _isDrawingModeActive.value = true
        _currentDrawingType.value = type
        _currentDrawingColor.value = color
        _currentDrawingPoints.value = emptyList()
    }

    override fun addDrawingPoint(lat: Double, lng: Double) {
        _currentDrawingPoints.value = _currentDrawingPoints.value + ShapeLatLng(lat, lng)
    }

    override fun undoDrawingPoint() {
        val pts = _currentDrawingPoints.value
        if (pts.isNotEmpty()) {
            _currentDrawingPoints.value = pts.dropLast(1)
        }
    }

    override fun finishDrawing() {
        val pts = _currentDrawingPoints.value
        val color = _currentDrawingColor.value
        val type = _currentDrawingType.value
        val newShape: DrawnShape? = when (type) {
            "polyline" -> if (pts.size >= 2) DrawnShape.Polyline("shape_${System.currentTimeMillis()}", color, pts) else null
            "polygon" -> if (pts.size >= 3) DrawnShape.Polygon("shape_${System.currentTimeMillis()}", color, pts) else null
            "marker" -> if (pts.isNotEmpty()) DrawnShape.Marker("shape_${System.currentTimeMillis()}", color, pts.last()) else null
            else -> null
        }
        if (newShape != null) {
            _drawnShapes.value = _drawnShapes.value + newShape
        }
        cancelDrawing()
    }

    override fun cancelDrawing() {
        _isDrawingModeActive.value = false
        _currentDrawingPoints.value = emptyList()
    }

    override fun setDrawingColor(color: String) {
        _currentDrawingColor.value = color
    }

    override fun setDrawingType(type: String) {
        _currentDrawingType.value = type
    }

    override fun showToast(message: String) {}
    override fun executeJs(script: String) {}

    companion object {
        fun samplePortals(): List<NativePortal> = listOf(
            NativePortal(
                guid = "portal_1",
                lat = 39.914,
                lng = 116.397,
                team = 1,
                level = 8,
                health = 100,
                resCount = 8,
                title = "Forbidden City Meridian Gate"
            ),
            NativePortal(
                guid = "portal_2",
                lat = 39.905,
                lng = 116.391,
                team = 2,
                level = 7,
                health = 85,
                resCount = 7,
                title = "Tiananmen Square Monument"
            ),
            NativePortal(
                guid = "portal_3",
                lat = 39.921,
                lng = 116.402,
                team = 3,
                level = 1,
                health = 40,
                resCount = 1,
                title = "Jingshan Park Hilltop"
            ),
            NativePortal(
                guid = "portal_4",
                lat = 39.918,
                lng = 116.388,
                team = 0,
                level = 0,
                health = 0,
                resCount = 0,
                title = "Beihai White Pagoda"
            )
        )

        fun sampleBookmarks(): List<BookmarkFolder> = listOf(
            BookmarkFolder(
                id = "idOthers",
                name = "General",
                bookmarks = listOf(
                    PortalBookmark("portal_1", "Forbidden City Meridian Gate", 39.914, 116.397, 1, 8),
                    PortalBookmark("portal_2", "Tiananmen Square Monument", 39.905, 116.391, 2, 7)
                )
            ),
            BookmarkFolder(
                id = "ops",
                name = "Anchor Farms",
                bookmarks = listOf(
                    PortalBookmark("portal_3", "Jingshan Park Hilltop", 39.921, 116.402, 3, 1)
                )
            )
        )

        fun sampleShapes(): List<DrawnShape> = listOf(
            DrawnShape.Polyline(
                id = "line_1",
                color = "#a24ac3",
                latLngs = listOf(ShapeLatLng(39.914, 116.397), ShapeLatLng(39.905, 116.391))
            ),
            DrawnShape.Marker(
                id = "marker_1",
                color = "#da3535",
                latLng = ShapeLatLng(39.921, 116.402),
                label = "Target Anchor"
            )
        )
    }
}
