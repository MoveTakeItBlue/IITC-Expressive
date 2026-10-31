package io.github.movetakeitblue.iitcexpressive.plugin.api

import io.github.movetakeitblue.iitcexpressive.model.ChatMessage
import io.github.movetakeitblue.iitcexpressive.model.NativeField
import io.github.movetakeitblue.iitcexpressive.model.NativeLink
import io.github.movetakeitblue.iitcexpressive.model.NativeMapEntities
import io.github.movetakeitblue.iitcexpressive.model.NativePortal
import io.github.movetakeitblue.iitcexpressive.model.PortalStatus
import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils
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
    initialShapes: List<DrawnShape> = sampleShapes(),
    initialKeys: Map<String, Int> = mapOf("portal_1" to 5, "portal_2" to 2)
) : IitcPluginHost {

    private val _visiblePortals = MutableStateFlow(initialPortals)
    override val visiblePortals: StateFlow<List<NativePortal>> = _visiblePortals.asStateFlow()

    private val _visibleLinks = MutableStateFlow(sampleLinks())
    override val visibleLinks: StateFlow<List<NativeLink>> = _visibleLinks.asStateFlow()

    private val _visibleFields = MutableStateFlow(sampleFields())
    override val visibleFields: StateFlow<List<NativeField>> = _visibleFields.asStateFlow()

    private val _visibleEntities = MutableStateFlow(
        NativeMapEntities(portals = initialPortals, links = sampleLinks(), fields = sampleFields())
    )
    override val visibleEntities: StateFlow<NativeMapEntities> = _visibleEntities.asStateFlow()

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

    private val _chatMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(sampleChatMessages())
    override val chatMessages: StateFlow<Map<String, List<ChatMessage>>> = _chatMessages.asStateFlow()

    // Scoreboard mock
    private val _regionScoreData = MutableStateFlow<RegionScoreData?>(sampleScoreboard())
    override val regionScoreData: StateFlow<RegionScoreData?> = _regionScoreData.asStateFlow()

    private val _isScoreboardLoading = MutableStateFlow(false)
    override val isScoreboardLoading: StateFlow<Boolean> = _isScoreboardLoading.asStateFlow()

    override fun requestRegionScore() {}

    // Periodic Refresh mock
    private val _periodicRefreshIntervalSeconds = MutableStateFlow(0)
    override val periodicRefreshIntervalSeconds: StateFlow<Int> = _periodicRefreshIntervalSeconds.asStateFlow()

    private val _timeToNextAutoRefreshSeconds = MutableStateFlow(0)
    override val timeToNextAutoRefreshSeconds: StateFlow<Int> = _timeToNextAutoRefreshSeconds.asStateFlow()

    override fun setPeriodicRefreshInterval(seconds: Int) {
        _periodicRefreshIntervalSeconds.value = seconds
    }

    override fun triggerMapRefresh() {}

    // Key Inventory mock
    private val _portalKeys = MutableStateFlow(initialKeys)
    override val portalKeys: StateFlow<Map<String, Int>> = _portalKeys.asStateFlow()

    override fun getPortalKeyCount(guid: String): Int = _portalKeys.value[guid] ?: 0

    override fun setPortalKeyCount(guid: String, count: Int) {
        val updated = _portalKeys.value.toMutableMap()
        if (count > 0) updated[guid] = count else updated.remove(guid)
        _portalKeys.value = updated
    }

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
        val now = PlatformUtils.currentTimeMillis()
        val newShape: DrawnShape? = when (type) {
            "polyline" -> if (pts.size >= 2) DrawnShape.Polyline("shape_$now", color, pts) else null
            "polygon" -> if (pts.size >= 3) DrawnShape.Polygon("shape_$now", color, pts) else null
            "marker" -> if (pts.isNotEmpty()) DrawnShape.Marker("shape_$now", color, pts.last()) else null
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

        fun sampleLinks(): List<NativeLink> = listOf(
            NativeLink("link_1", 1, 39.914, 116.397, 39.905, 116.391),
            NativeLink("link_2", 2, 39.905, 116.391, 39.921, 116.402)
        )

        fun sampleFields(): List<NativeField> = listOf(
            NativeField(
                "field_1", 1,
                listOf(
                    listOf(39.914, 116.397),
                    listOf(39.905, 116.391),
                    listOf(39.921, 116.402)
                )
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

        fun sampleScoreboard(): RegionScoreData = RegionScoreData(
            regionName = "AM01-GOLF-04",
            enlAvgScore = 248190L,
            resAvgScore = 312450L,
            checkpoints = listOf(
                CheckpointScore(1, 1729800000000L, 210000L, 305000L),
                CheckpointScore(2, 1729818000000L, 225000L, 310000L),
                CheckpointScore(3, 1729836000000L, 240000L, 315000L),
                CheckpointScore(4, 1729854000000L, 255000L, 320000L)
            ),
            topAgents = listOf(
                TopAgent(1, "AgentBluePrime", "RESISTANCE"),
                TopAgent(2, "GreenVanguard", "ENLIGHTENED"),
                TopAgent(3, "ResOpsSpecialist", "RESISTANCE"),
                TopAgent(4, "FieldMasterEnl", "ENLIGHTENED")
            )
        )

        fun sampleChatMessages(): Map<String, List<ChatMessage>> = mapOf(
            "all" to listOf(
                ChatMessage(
                    id = "msg_1",
                    time = PlatformUtils.currentTimeMillis() - 120000L,
                    formattedTime = "14:20",
                    sender = "AgentBluePrime",
                    team = "RESISTANCE",
                    text = "deployed an L8 Resonator on Forbidden City Meridian Gate",
                    portal = io.github.movetakeitblue.iitcexpressive.model.ChatPortal("Forbidden City Meridian Gate", 39.914, 116.397)
                ),
                ChatMessage(
                    id = "msg_2",
                    time = PlatformUtils.currentTimeMillis() - 60000L,
                    formattedTime = "14:21",
                    sender = "GreenVanguard",
                    team = "ENLIGHTENED",
                    text = "captured Tiananmen Square Monument",
                    portal = io.github.movetakeitblue.iitcexpressive.model.ChatPortal("Tiananmen Square Monument", 39.905, 116.391)
                )
            )
        )
    }
}
