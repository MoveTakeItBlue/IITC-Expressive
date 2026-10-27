package io.github.movetakeitblue.iitcexpressive.viewmodel

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.*
import io.github.movetakeitblue.iitcexpressive.bridge.EventsToIitc
import io.github.movetakeitblue.iitcexpressive.bridge.UrlConfig
import io.github.movetakeitblue.iitcexpressive.i18n.L
import io.github.movetakeitblue.iitcexpressive.manager.IITCManager
import io.github.movetakeitblue.iitcexpressive.model.*
import io.github.movetakeitblue.iitcexpressive.platform.PlatformLocationListener
import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils
import io.github.movetakeitblue.iitcexpressive.plugin.api.*
import io.github.movetakeitblue.iitcexpressive.plugin.core.NativePluginRegistry
import io.github.movetakeitblue.iitcexpressive.storage.AppStorage

enum class PanelPosition {
    HIDDEN,
    BOTTOM,
    MIDDLE,
    TOP
}

enum class LocationState {
    IDLE,
    LOCATING,
    FOLLOWING,
    ERROR
}

sealed class AppDialog {
    data class Alert(val title: String, val message: String, val onDismiss: () -> Unit) : AppDialog()
    data class Confirm(val title: String, val message: String, val onConfirm: () -> Unit, val onCancel: () -> Unit) : AppDialog()
    data class LocationShare(val lat: Double, val lng: Double, val title: String, val isPortal: Boolean, val guid: String?) : AppDialog()
    data class AddPlugin(val initialUrl: String? = null) : AppDialog()
    data class PluginInfo(val plugin: Plugin) : AppDialog()
    data class PlayerTrailInfo(val trail: PlayerTrail) : AppDialog()
}

val DEFAULT_HIGHLIGHTERS = listOf(
    "No Highlights",
    "Portal Level Color",
    "High Level Portals",
    "Needs Recharge",
    "Missing Resonators",
    "Portal Weakness",
    "Unclaimed Portals",
    "Hide Team Ownership"
)

class MainViewModel(
    val storage: AppStorage,
    val manager: IITCManager
) {
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // Navigation / Screen state
    private val _currentScreen = MutableStateFlow<String>("main") // "main", "settings", "plugins", "update_channel", "backup", "about", "licenses"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // URL & Web state
    private val _currentUrl = MutableStateFlow(UrlConfig.INGRESS_INTEL_MAP)
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _desktopMode = MutableStateFlow(false)
    val desktopMode: StateFlow<Boolean> = _desktopMode.asStateFlow()

    private val _zoomControl = MutableStateFlow(storage.getBoolean("zoom_control", false))
    val zoomControl: StateFlow<Boolean> = _zoomControl.asStateFlow()

    fun setZoomControl(enabled: Boolean) {
        _zoomControl.value = enabled
        storage.setBoolean("zoom_control", enabled)
    }

    private val _isWebviewLoaded = MutableStateFlow(false)
    val isWebviewLoaded: StateFlow<Boolean> = _isWebviewLoaded.asStateFlow()

    private val _isIitcLoaded = MutableStateFlow(false)
    val isIitcLoaded: StateFlow<Boolean> = _isIitcLoaded.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _pageTitle = MutableStateFlow("IITC Expressive")
    val pageTitle: StateFlow<String> = _pageTitle.asStateFlow()

    // Panel state
    private val _panelPosition = MutableStateFlow(PanelPosition.BOTTOM)
    val panelPosition: StateFlow<PanelPosition> = _panelPosition.asStateFlow()

    private val _activeTab = MutableStateFlow("quick") // "quick" | "layers"
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    private val _isMapDark = MutableStateFlow(true)
    val isMapDark: StateFlow<Boolean> = _isMapDark.asStateFlow()

    // Navigation panes & dialogs
    private val _currentPane = MutableStateFlow("map")
    val currentPane: StateFlow<String> = _currentPane.asStateFlow()

    private val _panes = MutableStateFlow(Pane.defaultPanes())
    val panes: StateFlow<List<Pane>> = _panes.asStateFlow()

    private val _dialogStack = MutableStateFlow<List<String>>(emptyList())
    val dialogStack: StateFlow<List<String>> = _dialogStack.asStateFlow()

    // Native Map state
    private val _isNativeMapEnabled = MutableStateFlow(storage.getBoolean("native_map_enabled", true))
    val isNativeMapEnabled: StateFlow<Boolean> = _isNativeMapEnabled.asStateFlow()

    fun setNativeMapEnabled(enabled: Boolean) {
        _isNativeMapEnabled.value = enabled
        storage.setBoolean("native_map_enabled", enabled)
    }

    // Fullscreen Map (Edge-to-edge extending behind status bar)
    private val _isFullscreenMap = MutableStateFlow(storage.getBoolean("fullscreen_map", false))
    val isFullscreenMap: StateFlow<Boolean> = _isFullscreenMap.asStateFlow()

    fun setFullscreenMap(enabled: Boolean) {
        _isFullscreenMap.value = enabled
        storage.setBoolean("fullscreen_map", enabled)
    }

    // Map Typography states (configurable in Settings)
    private val _mapTextColor = MutableStateFlow(storage.getString("map_text_color", "auto"))
    val mapTextColor: StateFlow<String> = _mapTextColor.asStateFlow()

    fun setMapTextColor(color: String) {
        _mapTextColor.value = color
        storage.setString("map_text_color", color)
    }

    private val _portalNameFontSize = MutableStateFlow(storage.getFloat("map_portal_font_size", 12f))
    val portalNameFontSize: StateFlow<Float> = _portalNameFontSize.asStateFlow()

    fun setPortalNameFontSize(size: Float) {
        _portalNameFontSize.value = size
        storage.setFloat("map_portal_font_size", size)
    }

    private val _agentNameFontSize = MutableStateFlow(storage.getFloat("map_agent_font_size", 11f))
    val agentNameFontSize: StateFlow<Float> = _agentNameFontSize.asStateFlow()

    fun setAgentNameFontSize(size: Float) {
        _agentNameFontSize.value = size
        storage.setFloat("map_agent_font_size", size)
    }

    private val _mapFontFamily = MutableStateFlow(storage.getString("map_font_family", "sans-serif"))
    val mapFontFamily: StateFlow<String> = _mapFontFamily.asStateFlow()

    fun setMapFontFamily(family: String) {
        _mapFontFamily.value = family
        storage.setString("map_font_family", family)
    }

    private val _mapFontBold = MutableStateFlow(storage.getBoolean("map_font_bold", false))
    val mapFontBold: StateFlow<Boolean> = _mapFontBold.asStateFlow()

    fun setMapFontBold(bold: Boolean) {
        _mapFontBold.value = bold
        storage.setBoolean("map_font_bold", bold)
    }

    private val _mapTextHalo = MutableStateFlow(storage.getBoolean("map_text_halo", false))
    val mapTextHalo: StateFlow<Boolean> = _mapTextHalo.asStateFlow()

    fun setMapTextHalo(enabled: Boolean) {
        _mapTextHalo.value = enabled
        storage.setBoolean("map_text_halo", enabled)
    }

    private val _playerIconScale = MutableStateFlow(storage.getFloat("map_player_icon_scale", 1.5f))
    val playerIconScale: StateFlow<Float> = _playerIconScale.asStateFlow()

    fun setPlayerIconScale(scale: Float) {
        _playerIconScale.value = scale
        storage.setFloat("map_player_icon_scale", scale)
    }

    private val _nativeMapEntities = MutableStateFlow(NativeMapEntities())
    val nativeMapEntities: StateFlow<NativeMapEntities> = _nativeMapEntities.asStateFlow()

    private val _nativeMapPortals = MutableStateFlow<List<NativePortal>>(emptyList())
    val nativeMapPortals: StateFlow<List<NativePortal>> = _nativeMapPortals.asStateFlow()

    // Map & Portal state
    private val _portalStatus = MutableStateFlow(PortalStatus())
    val portalStatus: StateFlow<PortalStatus> = _portalStatus.asStateFlow()

    private val _mapStatus = MutableStateFlow(MapStatus())
    val mapStatus: StateFlow<MapStatus> = _mapStatus.asStateFlow()

    // Native BaseMap state (Free, no API key required)
    private val _nativeBaseMap = MutableStateFlow(NativeBaseMap.fromId(storage.getString("native_basemap", "dark")))
    val nativeBaseMap: StateFlow<NativeBaseMap> = _nativeBaseMap.asStateFlow()

    fun selectNativeBaseMap(baseMap: NativeBaseMap) {
        _nativeBaseMap.value = baseMap
        storage.setString("native_basemap", baseMap.id)
        _isMapDark.value = baseMap.isDark
    }

    // Camera move event for Linked Portals & search navigation
    private val _cameraMoveEvent = MutableSharedFlow<Triple<Double, Double, Double>>(extraBufferCapacity = 1)
    val cameraMoveEvent: SharedFlow<Triple<Double, Double, Double>> = _cameraMoveEvent.asSharedFlow()

    fun zoomToLocation(lat: Double, lng: Double, zoom: Double = 17.0) {
        _cameraMoveEvent.tryEmit(Triple(lat, lng, zoom))
        evaluateJs("window.map && window.map.setView && window.map.setView([$lat, $lng], Math.round($zoom), { animate: true });")
    }

    fun selectLinkedPortal(linked: LinkedPortal) {
        if (linked.lat != 0.0 && linked.lng != 0.0) {
            zoomToLocation(linked.lat, linked.lng, 17.0)
        }
        if (linked.guid.isNotBlank()) {
            onPortalTapped(linked.guid)
        }
    }

    // Player Activity Tracker state
    private val _isPlayerTrackerEnabled = MutableStateFlow(storage.getBoolean("player_tracker_enabled", false))
    val isPlayerTrackerEnabled: StateFlow<Boolean> = _isPlayerTrackerEnabled.asStateFlow()

    fun setPlayerTrackerEnabled(enabled: Boolean) {
        _isPlayerTrackerEnabled.value = enabled
        storage.setBoolean("player_tracker_enabled", enabled)
        if (enabled) {
            updatePlayerTrails(_chatMessages.value)
        }
    }

    private val _playerTrails = MutableStateFlow<List<PlayerTrail>>(emptyList())
    val playerTrails: StateFlow<List<PlayerTrail>> = _playerTrails.asStateFlow()

    private val _highlighters = MutableStateFlow(DEFAULT_HIGHLIGHTERS)
    val highlighters: StateFlow<List<String>> = _highlighters.asStateFlow()

    private val _selectedHighlighter = MutableStateFlow(storage.getString("selected_highlighter", "No Highlights"))
    val selectedHighlighter: StateFlow<String> = _selectedHighlighter.asStateFlow()

    private val _baseLayers = MutableStateFlow<List<MapLayer>>(emptyList())
    val baseLayers: StateFlow<List<MapLayer>> = _baseLayers.asStateFlow()

    private val _selectedBaseLayer = MutableStateFlow(0)
    val selectedBaseLayer: StateFlow<Int> = _selectedBaseLayer.asStateFlow()

    private val _overlayLayers = MutableStateFlow<List<MapLayer>>(emptyList())
    val overlayLayers: StateFlow<List<MapLayer>> = _overlayLayers.asStateFlow()

    // Debug console & logs
    private val _isDebugActive = MutableStateFlow(false)
    val isDebugActive: StateFlow<Boolean> = _isDebugActive.asStateFlow()

    private val _logs = MutableStateFlow<List<LogItem>>(emptyList())
    val logs: StateFlow<List<LogItem>> = _logs.asStateFlow()

    // Location state
    private val _locationState = MutableStateFlow(LocationState.IDLE)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    // Active popup/dialog
    private val _currentDialog = MutableStateFlow<AppDialog?>(null)
    val currentDialog: StateFlow<AppDialog?> = _currentDialog.asStateFlow()

    // Clipboard link detection
    private val _hasClipboardLink = MutableStateFlow(false)
    val hasClipboardLink: StateFlow<Boolean> = _hasClipboardLink.asStateFlow()

    // Secondary sheets & FAB menu state
    private val _isLayersSheetOpen = MutableStateFlow(false)
    val isLayersSheetOpen: StateFlow<Boolean> = _isLayersSheetOpen.asStateFlow()

    private val _isChatSheetOpen = MutableStateFlow(false)
    val isChatSheetOpen: StateFlow<Boolean> = _isChatSheetOpen.asStateFlow()

    private val _isPortalDetailsExpanded = MutableStateFlow(false)
    val isPortalDetailsExpanded: StateFlow<Boolean> = _isPortalDetailsExpanded.asStateFlow()

    private val _isFabMenuExpanded = MutableStateFlow(false)
    val isFabMenuExpanded: StateFlow<Boolean> = _isFabMenuExpanded.asStateFlow()

    // Structured COMM Chat Messages by channel ("all", "faction", "alerts")
    private val _chatMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf("all" to emptyList(), "faction" to emptyList(), "alerts" to emptyList())
    )
    val chatMessages: StateFlow<Map<String, List<ChatMessage>>> = _chatMessages.asStateFlow()

    fun openLayersSheet() {
        _isFabMenuExpanded.value = false
        _isLayersSheetOpen.value = true
    }

    fun closeLayersSheet() {
        _isLayersSheetOpen.value = false
    }

    fun openChatSheet() {
        _isFabMenuExpanded.value = false
        _isChatSheetOpen.value = true
        requestChat("all")
    }

    fun closeChatSheet() {
        _isChatSheetOpen.value = false
    }

    fun toggleFabMenu() {
        _isFabMenuExpanded.value = !_isFabMenuExpanded.value
    }

    fun closeFabMenu() {
        _isFabMenuExpanded.value = false
    }

    // Active native plugin sheet (e.g. "bookmarks", "portals-list", "draw-tools", or null)
    private val _activePluginSheet = MutableStateFlow<String?>(null)
    val activePluginSheet: StateFlow<String?> = _activePluginSheet.asStateFlow()

    // Native Draw Tools state
    private val _drawnShapes = MutableStateFlow<List<DrawnShape>>(emptyList())
    val drawnShapes: StateFlow<List<DrawnShape>> = _drawnShapes.asStateFlow()

    private val _isDrawingModeActive = MutableStateFlow(false)
    val isDrawingModeActive: StateFlow<Boolean> = _isDrawingModeActive.asStateFlow()

    private val _currentDrawingType = MutableStateFlow("polyline")
    val currentDrawingType: StateFlow<String> = _currentDrawingType.asStateFlow()

    private val _currentDrawingColor = MutableStateFlow("#a24ac3")
    val currentDrawingColor: StateFlow<String> = _currentDrawingColor.asStateFlow()

    private val _currentDrawingPoints = MutableStateFlow<List<ShapeLatLng>>(emptyList())
    val currentDrawingPoints: StateFlow<List<ShapeLatLng>> = _currentDrawingPoints.asStateFlow()

    // Native Bookmarks state
    private val _bookmarkFolders = MutableStateFlow<List<BookmarkFolder>>(BookmarkSerializer.defaultFolders())
    val bookmarkFolders: StateFlow<List<BookmarkFolder>> = _bookmarkFolders.asStateFlow()

    val pluginHost: IitcPluginHost = object : IitcPluginHost {
        override val visiblePortals: StateFlow<List<NativePortal>>
            get() = _nativeMapPortals.asStateFlow()
        override val selectedPortalGuid: StateFlow<String?>
            get() = MutableStateFlow(_portalStatus.value.guid.takeIf { _portalStatus.value.hasSelected }).asStateFlow()
        override val selectedPortal: StateFlow<PortalStatus>
            get() = _portalStatus.asStateFlow()
        override val drawnShapes: StateFlow<List<DrawnShape>>
            get() = _drawnShapes.asStateFlow()
        override val bookmarkFolders: StateFlow<List<BookmarkFolder>>
            get() = _bookmarkFolders.asStateFlow()
        override val isDrawingModeActive: StateFlow<Boolean>
            get() = _isDrawingModeActive.asStateFlow()
        override val currentDrawingType: StateFlow<String>
            get() = _currentDrawingType.asStateFlow()
        override val currentDrawingColor: StateFlow<String>
            get() = _currentDrawingColor.asStateFlow()
        override val currentDrawingPoints: StateFlow<List<ShapeLatLng>>
            get() = _currentDrawingPoints.asStateFlow()

        override fun flyTo(lat: Double, lng: Double, zoom: Double?) {
            this@MainViewModel.flyTo(lat, lng, zoom)
        }

        override fun selectPortal(guid: String) {
            this@MainViewModel.selectPortalByGuid(guid)
        }

        override fun deselectPortal() {
            this@MainViewModel.deselectPortal()
        }

        override fun saveDrawnShapes(shapes: List<DrawnShape>) {
            this@MainViewModel.saveDrawnShapes(shapes)
        }

        override fun saveBookmarkFolders(folders: List<BookmarkFolder>) {
            this@MainViewModel.saveBookmarkFolders(folders)
        }

        override fun togglePortalBookmark(portal: NativePortal): Boolean {
            return this@MainViewModel.togglePortalBookmark(portal)
        }

        override fun isPortalBookmarked(guid: String): Boolean {
            return this@MainViewModel.isPortalBookmarked(guid)
        }

        override fun startDrawing(type: String, color: String) {
            this@MainViewModel.startDrawing(type, color)
        }

        override fun addDrawingPoint(lat: Double, lng: Double) {
            this@MainViewModel.addDrawingPoint(lat, lng)
        }

        override fun undoDrawingPoint() {
            this@MainViewModel.undoDrawingPoint()
        }

        override fun finishDrawing() {
            this@MainViewModel.finishDrawing()
        }

        override fun cancelDrawing() {
            this@MainViewModel.cancelDrawing()
        }

        override fun setDrawingColor(color: String) {
            _currentDrawingColor.value = color
        }

        override fun setDrawingType(type: String) {
            _currentDrawingType.value = type
        }

        override fun showToast(message: String) {
            PlatformUtils.showToast(message)
        }

        override fun executeJs(script: String) {
            evaluateJs(script)
        }
    }

    fun openPluginSheet(id: String) {
        _isFabMenuExpanded.value = false
        _activePluginSheet.value = id
    }

    fun closePluginSheet() {
        _activePluginSheet.value = null
    }

    fun togglePluginSheet(id: String) {
        if (_activePluginSheet.value == id) {
            _activePluginSheet.value = null
        } else {
            openPluginSheet(id)
        }
    }

    fun flyTo(lat: Double, lng: Double, zoom: Double? = null) {
        evaluateJs(EventsToIitc.setMapCenterZoom(lat, lng, zoom))
    }

    fun selectPortalByGuid(guid: String) {
        evaluateJs(EventsToIitc.selectPortal(guid))
    }

    fun saveDrawnShapes(shapes: List<DrawnShape>) {
        _drawnShapes.value = shapes
        val jsonStr = DrawnShape.toIitcJson(shapes)
        storage.setString("plugin-draw-tools-layer", jsonStr)
        evaluateJs(EventsToIitc.syncPluginStorage("plugin-draw-tools-layer", jsonStr))
    }

    fun saveBookmarkFolders(folders: List<BookmarkFolder>) {
        _bookmarkFolders.value = folders
        val jsonStr = BookmarkSerializer.toIitcJson(folders)
        storage.setString("plugin-bookmarks-portals-data", jsonStr)
        evaluateJs(EventsToIitc.syncPluginStorage("plugin-bookmarks-portals-data", jsonStr))
    }

    fun togglePortalBookmark(portal: NativePortal): Boolean {
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
        saveBookmarkFolders(folders)
        return updated
    }

    fun isPortalBookmarked(guid: String): Boolean {
        if (guid.isBlank()) return false
        return _bookmarkFolders.value.any { folder -> folder.bookmarks.any { it.guid == guid } }
    }

    fun startDrawing(type: String = "polyline", color: String = "#a24ac3") {
        _isDrawingModeActive.value = true
        _currentDrawingType.value = type
        _currentDrawingColor.value = color
        _currentDrawingPoints.value = emptyList()
    }

    fun addDrawingPoint(lat: Double, lng: Double) {
        if (!_isDrawingModeActive.value) return
        if (_currentDrawingType.value == "marker") {
            _currentDrawingPoints.value = listOf(ShapeLatLng(lat, lng))
            finishDrawing()
        } else {
            _currentDrawingPoints.value = _currentDrawingPoints.value + ShapeLatLng(lat, lng)
        }
    }

    fun undoDrawingPoint() {
        val pts = _currentDrawingPoints.value
        if (pts.isNotEmpty()) {
            _currentDrawingPoints.value = pts.dropLast(1)
        }
    }

    fun finishDrawing() {
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
            saveDrawnShapes(_drawnShapes.value + newShape)
            PlatformUtils.showToast("Saved drawn ${newShape::class.simpleName}")
        }
        cancelDrawing()
    }

    fun cancelDrawing() {
        _isDrawingModeActive.value = false
        _currentDrawingPoints.value = emptyList()
    }

    fun expandPortalDetails() {
        _isPortalDetailsExpanded.value = true
    }

    fun collapsePortalDetails() {
        _isPortalDetailsExpanded.value = false
    }

    fun deselectPortal() {
        _portalStatus.value = PortalStatus()
        _isPortalDetailsExpanded.value = false
        evaluateJs("if (window.renderPortalDetails) window.renderPortalDetails(null);")
    }

    fun selectPortalByLatLng(lat: Double, lng: Double) {
        evaluateJs("if (window.selectPortalByLatLng) window.selectPortalByLatLng($lat, $lng);")
        closeChatSheet()
    }

    fun sendChatMessage(channel: String, message: String) {
        if (message.isBlank()) return
        val escaped = message.replace("'", "\\'").replace("\n", " ")
        evaluateJs("""
            (function() {
                if (window.chat && window.chat.postMsg) {
                    var inp = document.querySelector('#chatinput input');
                    if (inp) inp.value = '$escaped';
                    window.chat.postMsg('$channel');
                }
            })();
        """.trimIndent())
    }

    fun requestChat(channel: String) {
        evaluateJs("""
            (function() {
                if (window.idleReset) window.idleReset();
                if (window.chat) {
                    if (window.chat.chooseTab) window.chat.chooseTab('$channel');
                    if ('$channel' === 'faction') {
                        if (window.chat.requestFaction) window.chat.requestFaction(false);
                        else if (window.IITC && window.IITC.comm && window.IITC.comm.requestChannel) window.IITC.comm.requestChannel('faction', false);
                    } else if ('$channel' === 'alerts') {
                        if (window.chat.requestAlerts) window.chat.requestAlerts(false);
                        else if (window.IITC && window.IITC.comm && window.IITC.comm.requestChannel) window.IITC.comm.requestChannel('alerts', false);
                    } else {
                        if (window.chat.requestPublic) window.chat.requestPublic(false);
                        else if (window.IITC && window.IITC.comm && window.IITC.comm.requestChannel) window.IITC.comm.requestChannel('all', false);
                    }
                    if (window.__sendChat) window.__sendChat('$channel');
                }
            })();
        """.trimIndent())
    }

    fun getSavedMapPosition(): Triple<Double, Double, Double>? {
        val lat = storage.getString("saved_map_lat", "").toDoubleOrNull() ?: return null
        val lng = storage.getString("saved_map_lng", "").toDoubleOrNull() ?: return null
        val zoom = storage.getString("saved_map_zoom", "").toDoubleOrNull() ?: return null
        return Triple(lat, lng, zoom)
    }

    private var lastCameraSyncJob: Job? = null
    fun onNativeMapCameraChanged(lat: Double, lng: Double, zoom: Double) {
        // Persist map position and zoom level for session restore
        storage.setString("saved_map_lat", lat.toString())
        storage.setString("saved_map_lng", lng.toString())
        storage.setString("saved_map_zoom", zoom.toString())

        lastCameraSyncJob?.cancel()
        lastCameraSyncJob = scope.launch {
            delay(150)
            evaluateJs("""
                (function() {
                    if (window.map) {
                        window.__syncFromNative = true;
                        window.map.setView([$lat, $lng], Math.round($zoom), { animate: false });
                        window.__syncFromNative = false;
                    }
                })();
            """.trimIndent())
        }
    }

    fun onPortalTapped(guid: String) {
        evaluateJs("""
            (function() {
                if (window.renderPortalDetails) {
                    window.renderPortalDetails('$guid');
                } else if (window.selectPortal) {
                    window.selectPortal('$guid');
                }
            })();
        """.trimIndent())
    }

    // Pending JS script to execute in webview
    // Queue of JS scripts to execute in webview
    val jsChannel = kotlinx.coroutines.channels.Channel<String>(kotlinx.coroutines.channels.Channel.UNLIMITED)

    // Reload trigger
    private val _reloadTrigger = MutableStateFlow(0)
    val reloadTrigger: StateFlow<Int> = _reloadTrigger.asStateFlow()

    init {
        val savedDesktop = storage.getBoolean("desktop_mode", false)
        _desktopMode.value = savedDesktop

        val showLocation = storage.getBoolean("show_location", false)
        if (showLocation) {
            startLocationTracking()
        }

        val savedDraw = storage.getString("plugin-draw-tools-layer", "")
        if (savedDraw.isNotBlank()) {
            val parsed = DrawnShape.parseIitcJson(savedDraw)
            if (parsed.isNotEmpty()) _drawnShapes.value = parsed
        }

        val savedBookmarks = storage.getString("plugin-bookmarks-portals-data", "")
        if (savedBookmarks.isNotBlank()) {
            val parsed = BookmarkSerializer.parseIitcJson(savedBookmarks)
            if (parsed.isNotEmpty()) _bookmarkFolders.value = parsed
        }

        val savedBaseMap = NativeBaseMap.fromId(storage.getString("native_basemap", "dark"))
        _nativeBaseMap.value = savedBaseMap
        _isMapDark.value = savedBaseMap.isDark

        scope.launch {
            manager.plugins.collect { pluginsMap ->
                val pat = pluginsMap["player-activity-tracker"]
                if (pat != null && pat.isEnabled) {
                    _isPlayerTrackerEnabled.value = true
                }

                val dynamicList = DEFAULT_HIGHLIGHTERS.toMutableList()
                val pluginHighlighters = mapOf(
                    "highlight-portal-history" to "Portal History",
                    "highlight-forgotten" to "Inactive Portals",
                    "highlight-ornaments" to "Portals with Ornaments",
                    "highlight-portals-my-level" to "Portals Near My Level"
                )
                for ((uid, name) in pluginHighlighters) {
                    val p = pluginsMap[uid]
                    if (p != null && p.isEnabled && !dynamicList.contains(name)) {
                        dynamicList.add(name)
                    }
                }
                for (h in _highlighters.value) {
                    if (!dynamicList.contains(h)) {
                        dynamicList.add(h)
                    }
                }
                _highlighters.value = dynamicList
            }
        }
    }

    private fun updatePlayerTrails(messagesMap: Map<String, List<ChatMessage>>) {
        val timeWindow = PlatformUtils.currentTimeMillis() - (24 * 3600 * 1000L)
        val allMsgs = messagesMap.values.flatten()
            .filter { it.portal != null && it.sender.isNotBlank() && it.time >= timeWindow }

        val grouped = allMsgs.groupBy { it.sender }
        val trails = mutableListOf<PlayerTrail>()

        for ((sender, msgs) in grouped) {
            val sortedMsgs = msgs.sortedBy { it.time }
            val points = mutableListOf<PlayerActivityPoint>()
            for (m in sortedMsgs) {
                val p = m.portal ?: continue
                if (p.lat == 0.0 && p.lng == 0.0) continue
                val last = points.lastOrNull()
                if (last == null || last.lat != p.lat || last.lng != p.lng) {
                    points.add(
                        PlayerActivityPoint(
                            lat = p.lat,
                            lng = p.lng,
                            portalName = p.name,
                            time = m.time,
                            formattedTime = m.formattedTime
                        )
                    )
                }
            }
            if (points.isNotEmpty()) {
                val latest = points.last()
                val team = sortedMsgs.last().team
                trails.add(
                    PlayerTrail(
                        sender = sender,
                        team = team,
                        points = points,
                        lastSeenTime = latest.time,
                        latestLat = latest.lat,
                        latestLng = latest.lng,
                        latestPortalName = latest.portalName
                    )
                )
            }
        }
        _playerTrails.value = trails
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (_currentScreen.value != "main") {
            if (_currentScreen.value in listOf("plugins", "update_channel", "backup", "about", "licenses")) {
                _currentScreen.value = "settings"
            } else {
                _currentScreen.value = "main"
            }
            return true
        }
        return handleBackPress()
    }

    fun setPanelPosition(pos: PanelPosition) {
        _panelPosition.value = pos
    }

    fun togglePanel() {
        if (_panelPosition.value == PanelPosition.BOTTOM) {
            _panelPosition.value = PanelPosition.MIDDLE
        } else if (_panelPosition.value == PanelPosition.MIDDLE || _panelPosition.value == PanelPosition.TOP) {
            _panelPosition.value = PanelPosition.BOTTOM
        }
    }

    fun openPanel() {
        if (_panelPosition.value == PanelPosition.BOTTOM || _panelPosition.value == PanelPosition.HIDDEN) {
            _panelPosition.value = PanelPosition.MIDDLE
        }
    }

    fun closePanel() {
        _panelPosition.value = PanelPosition.BOTTOM
    }

    fun restorePanel() {
        _panelPosition.value = PanelPosition.BOTTOM
    }

    fun hidePanel() {
        _panelPosition.value = PanelPosition.HIDDEN
    }

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
        if (_panelPosition.value == PanelPosition.BOTTOM || _panelPosition.value == PanelPosition.HIDDEN) {
            _panelPosition.value = PanelPosition.MIDDLE
        }
    }

    fun setDialog(dialog: AppDialog?) {
        _currentDialog.value = dialog
    }

    fun checkClipboard() {
        _hasClipboardLink.value = PlatformUtils.hasClipboardUrl()
    }

    fun handlePasteClipboard() {
        val text = PlatformUtils.readClipboardText() ?: return
        if (text.startsWith("http://", ignoreCase = true) || text.startsWith("https://", ignoreCase = true)) {
            if (text.contains(".user.js")) {
                setDialog(AppDialog.AddPlugin(initialUrl = text))
            } else if (text.contains("ingress.com") || text.contains("iitc")) {
                loadUrl(text)
            }
        }
    }

    fun loadUrl(url: String) {
        val target = UrlConfig.addViewportParam(url, _desktopMode.value)
        _currentUrl.value = target
    }

    fun reloadWebView(clearCache: Boolean = false) {
        if (clearCache) {
            PlatformUtils.clearCookies {}
        }
        _reloadTrigger.value += 1
    }

    fun evaluateJs(code: String) {
        jsChannel.trySend(code)
    }

    fun setWebviewLoaded(loaded: Boolean) {
        _isWebviewLoaded.value = loaded
    }

    fun setProgress(prog: Float) {
        _progress.value = prog
    }

    fun setPageTitle(title: String) {
        _pageTitle.value = title
    }

    fun toggleDebugMode() {
        _isDebugActive.value = !_isDebugActive.value
    }

    fun addLog(type: String, message: String, source: String = "webview") {
        val item = LogItem(
            type = type,
            message = message,
            timestamp = PlatformUtils.currentTimeMillis().toString(),
            source = source,
            id = "${PlatformUtils.currentTimeMillis()}_${message.hashCode()}"
        )
        val list = _logs.value.toMutableList()
        list.add(item)
        if (list.size > 500) list.removeAt(0)
        _logs.value = list
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun executeDebugCommand(command: String) {
        addLog("info", ">>> $command", "user")
        val escaped = command.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
        evaluateJs("window.nsWebViewBridge?._listeners?.['console:execute']?.forEach(cb => cb({command: \"$escaped\"})) || eval(\"$escaped\");")
    }

    fun selectHighlighter(name: String) {
        _selectedHighlighter.value = name
        storage.setString("selected_highlighter", name)
        evaluateJs(EventsToIitc.changePortalHighlights(name))
    }

    fun selectBaseLayer(layer: MapLayer) {
        _selectedBaseLayer.value = layer.layerId
        evaluateJs(EventsToIitc.showLayer(layer.layerId, true))
    }

    fun toggleOverlayLayer(layer: MapLayer) {
        val newActive = !layer.active
        val updated = _overlayLayers.value.map {
            if (it.layerId == layer.layerId) it.copy(active = newActive) else it
        }
        _overlayLayers.value = updated
        evaluateJs(EventsToIitc.showLayer(layer.layerId, newActive))
    }

    fun togglePortalLevelOverlay(index: Int) {
        if (index in _overlayLayers.value.indices) {
            toggleOverlayLayer(_overlayLayers.value[index])
        }
    }

    fun switchToPane(name: String) {
        _currentPane.value = name
        evaluateJs(EventsToIitc.switchToPane(name))
        if (name == "map") {
            evaluateJs(EventsToIitc.removePaneSafeAreaInsets())
        } else {
            evaluateJs(EventsToIitc.applyPaneSafeAreaInsets())
        }
        closePanel()
    }

    fun setFollowMode(follow: Boolean) {
        _locationState.value = if (follow) LocationState.FOLLOWING else LocationState.IDLE
    }

    fun triggerUserLocate() {
        _locationState.value = LocationState.LOCATING
        val persistentZoom = storage.getBoolean("persistent_zoom", false)
        PlatformUtils.requestSingleLocationFix(object : PlatformLocationListener {
            override fun onLocation(lat: Double, lng: Double, accuracy: Double) {
                _locationState.value = LocationState.FOLLOWING
                evaluateJs(EventsToIitc.userLocationLocate(lat, lng, accuracy, persistentZoom))
            }

            override fun onOrientation(direction: Double?) {
                evaluateJs(EventsToIitc.userLocationOrientation(direction))
            }

            override fun onError(message: String) {
                _locationState.value = LocationState.ERROR
                scope.launch {
                    delay(3000)
                    if (_locationState.value == LocationState.ERROR) {
                        _locationState.value = LocationState.IDLE
                    }
                }
            }
        })
    }

    private fun startLocationTracking() {
        PlatformUtils.startLocationTracking(object : PlatformLocationListener {
            override fun onLocation(lat: Double, lng: Double, accuracy: Double) {
                evaluateJs(EventsToIitc.userLocationUpdate(lat, lng))
            }

            override fun onOrientation(direction: Double?) {
                evaluateJs(EventsToIitc.userLocationOrientation(direction))
            }

            override fun onError(message: String) {}
        })
    }

    fun handleBackPress(): Boolean {
        // 0. If plugin sheet is open, close it
        if (_activePluginSheet.value != null) {
            _activePluginSheet.value = null
            return true
        }

        // 0.1 If drawing mode is active, cancel drawing
        if (_isDrawingModeActive.value) {
            cancelDrawing()
            return true
        }

        // 1. If fab menu is expanded, collapse it
        if (_isFabMenuExpanded.value) {
            _isFabMenuExpanded.value = false
            return true
        }

        // 2. If portal details expanded, collapse to peek
        if (_isPortalDetailsExpanded.value) {
            _isPortalDetailsExpanded.value = false
            return true
        }

        // 3. If layers sheet open, close it
        if (_isLayersSheetOpen.value) {
            _isLayersSheetOpen.value = false
            return true
        }

        // 4. If chat sheet open, close it
        if (_isChatSheetOpen.value) {
            _isChatSheetOpen.value = false
            return true
        }

        // 5. If portal is selected, deselect it
        if (_portalStatus.value.hasSelected) {
            deselectPortal()
            return true
        }

        // 6. If debug console active, close it
        if (_isDebugActive.value) {
            _isDebugActive.value = false
            return true
        }

        // 7. If IITC dialog active, close top dialog
        val dialogs = _dialogStack.value
        if (dialogs.isNotEmpty()) {
            val top = dialogs.last()
            _dialogStack.value = dialogs.dropLast(1)
            evaluateJs(EventsToIitc.closeDialog(top))
            return true
        }

        // 8. If panel open (MIDDLE or TOP), collapse to BOTTOM
        if (_panelPosition.value == PanelPosition.MIDDLE || _panelPosition.value == PanelPosition.TOP) {
            _panelPosition.value = PanelPosition.BOTTOM
            return true
        }

        // 9. If not on map pane, switch to map
        if (_currentPane.value != "map") {
            switchToPane("map")
            return true
        }

        return false
    }

    // Process incoming bridge events from WebView
    fun handleBridgeMessage(eventName: String, data: JsonElement) {
        when (eventName) {
            "intentPosLink" -> {
                val obj = data.jsonObject
                val lat = obj["lat"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                val lng = obj["lng"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                val title = obj["title"]?.jsonPrimitive?.contentOrNull ?: ""
                val isPortal = obj["isPortal"]?.jsonPrimitive?.booleanOrNull ?: false
                val guid = obj["guid"]?.jsonPrimitive?.contentOrNull
                setDialog(AppDialog.LocationShare(lat, lng, title, isPortal, guid))
            }
            "shareString" -> {
                val str = data.jsonObject["str"]?.jsonPrimitive?.contentOrNull ?: ""
                PlatformUtils.shareText(str, "IITC Share")
            }
            "copy" -> {
                val text = data.jsonObject["s"]?.jsonPrimitive?.contentOrNull ?: ""
                PlatformUtils.copyToClipboard(text, L("share.toast.coordinates_copied"))
            }
            "switchToPane" -> {
                val id = data.jsonObject["id"]?.jsonPrimitive?.contentOrNull ?: "map"
                _currentPane.value = id
            }
            "bootFinished" -> {
                _isIitcLoaded.value = true
                addLog("info", "IITC boot finished", "iitc")
                evaluateJs(EventsToIitc.setSafeAreaInsets(top = 0f, bottom = 108f, left = 0f, right = 0f))
                if (_drawnShapes.value.isNotEmpty()) {
                    val dt = DrawnShape.toIitcJson(_drawnShapes.value)
                    evaluateJs(EventsToIitc.syncPluginStorage("plugin-draw-tools-layer", dt))
                }
                if (_bookmarkFolders.value.any { it.bookmarks.isNotEmpty() }) {
                    val bm = BookmarkSerializer.toIitcJson(_bookmarkFolders.value)
                    evaluateJs(EventsToIitc.syncPluginStorage("plugin-bookmarks-portals-data", bm))
                }
            }
            "syncPluginData" -> {
                val key = data.jsonObject["key"]?.jsonPrimitive?.contentOrNull ?: ""
                val value = data.jsonObject["value"]?.jsonPrimitive?.contentOrNull ?: ""
                if (key == "plugin-draw-tools-layer" && value.isNotBlank()) {
                    if (_drawnShapes.value.isEmpty()) {
                        val parsed = DrawnShape.parseIitcJson(value)
                        if (parsed.isNotEmpty()) {
                            _drawnShapes.value = parsed
                            storage.setString("plugin-draw-tools-layer", value)
                        }
                    }
                } else if (key == "plugin-bookmarks-portals-data" && value.isNotBlank()) {
                    val isDefault = _bookmarkFolders.value.size <= 1 && _bookmarkFolders.value.all { it.bookmarks.isEmpty() }
                    if (isDefault) {
                        val parsed = BookmarkSerializer.parseIitcJson(value)
                        if (parsed.any { it.bookmarks.isNotEmpty() }) {
                            _bookmarkFolders.value = parsed
                            storage.setString("plugin-bookmarks-portals-data", value)
                        }
                    }
                }
            }
            "setLayers" -> {
                try {
                    val obj = data.jsonObject
                    val baseLayerStr = obj["base_layer"]?.jsonPrimitive?.content ?: "[]"
                    val overlayLayerStr = obj["overlay_layer"]?.jsonPrimitive?.content ?: "[]"

                    val baseArray = json.parseToJsonElement(baseLayerStr).jsonArray
                    val overlayArray = json.parseToJsonElement(overlayLayerStr).jsonArray

                    val baseList = baseArray.mapIndexed { idx, it ->
                        val itemObj = it.jsonObject
                        val id = itemObj["layerId"]?.jsonPrimitive?.intOrNull ?: idx
                        val name = itemObj["name"]?.jsonPrimitive?.contentOrNull ?: "Base $idx"
                        val active = itemObj["active"]?.jsonPrimitive?.booleanOrNull ?: (idx == 0)
                        MapLayer(layerId = id, name = name, active = active)
                    }

                    val overlayList = overlayArray.mapIndexed { idx, it ->
                        val itemObj = it.jsonObject
                        val id = itemObj["layerId"]?.jsonPrimitive?.intOrNull ?: idx
                        val name = itemObj["name"]?.jsonPrimitive?.contentOrNull ?: "Layer $idx"
                        val active = itemObj["active"]?.jsonPrimitive?.booleanOrNull ?: false
                        MapLayer(layerId = id, name = name, active = active)
                    }

                    _baseLayers.value = baseList
                    _overlayLayers.value = overlayList
                } catch (e: Exception) {
                    println("setLayers parse error: ${e.message}")
                }
            }
            "addPortalHighlighter" -> {
                val name = data.jsonObject["name"]?.jsonPrimitive?.contentOrNull ?: return
                if (!_highlighters.value.contains(name)) {
                    _highlighters.value = _highlighters.value + name
                }
            }
            "setActiveHighlighter" -> {
                val name = data.jsonObject["name"]?.jsonPrimitive?.contentOrNull ?: return
                _selectedHighlighter.value = name
            }
            "addPane" -> {
                val obj = data.jsonObject
                val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: return
                val label = obj["label"]?.jsonPrimitive?.contentOrNull ?: name
                val icon = obj["icon"]?.jsonPrimitive?.contentOrNull ?: "ms-map"
                if (_panes.value.none { it.name == name }) {
                    _panes.value = _panes.value + Pane(name, label, icon)
                }
            }
            "setFollowMode" -> {
                val follow = data.jsonObject["follow"]?.jsonPrimitive?.booleanOrNull ?: false
                setFollowMode(follow)
            }
            "setMapDarkMode" -> {
                val isDark = data.jsonObject["isDark"]?.jsonPrimitive?.booleanOrNull ?: true
                _isMapDark.value = isDark
                // Status bar contrast is tied to UI surface container, not leaflet tile dark filter
            }
            "setPortalStatus" -> {
                try {
                    val obj = data as? JsonObject
                    if (obj == null) {
                        _portalStatus.value = PortalStatus()
                    } else {
                        val guid = obj["guid"]?.jsonPrimitive?.contentOrNull
                        if (guid.isNullOrBlank()) {
                            _portalStatus.value = PortalStatus()
                        } else {
                        val team = obj["team"]?.jsonPrimitive?.contentOrNull ?: "N"
                        val level = obj["level"]?.jsonPrimitive?.intOrNull ?: 0
                        val title = obj["title"]?.jsonPrimitive?.contentOrNull ?: "No portal selected"
                        val health = obj["health"]?.jsonPrimitive?.intOrNull ?: 0
                        val levelColor = obj["levelColor"]?.jsonPrimitive?.contentOrNull
                        val isLoading = obj["isLoading"]?.jsonPrimitive?.booleanOrNull ?: false

                        val resonatorsElem = obj["resonators"]
                        val resonatorsList = if (resonatorsElem is JsonArray) {
                            resonatorsElem.mapIndexed { idx, resElem ->
                                val rObj = resElem.jsonObject
                                val prevOwner = _portalStatus.value.resonators?.getOrNull(idx)?.owner
                                Resonator(
                                    level = rObj["level"]?.jsonPrimitive?.intOrNull ?: 0,
                                    levelColor = rObj["levelColor"]?.jsonPrimitive?.contentOrNull ?: "#808080",
                                    healthPct = rObj["healthPct"]?.jsonPrimitive?.intOrNull ?: 0,
                                    energy = rObj["energy"]?.jsonPrimitive?.intOrNull ?: 0,
                                    owner = rObj["owner"]?.jsonPrimitive?.contentOrNull?.ifBlank { null } ?: prevOwner
                                )
                            }
                        } else null

                        val prev = _portalStatus.value
                        val isSamePortal = prev.guid == guid
                        _portalStatus.value = PortalStatus(
                            guid = guid,
                            team = team,
                            level = level,
                            isNeutral = team == "N",
                            title = title,
                            health = health,
                            resonators = resonatorsList ?: if (isSamePortal) prev.resonators else null,
                            levelColor = levelColor,
                            isLoading = isLoading,
                            image = if (isSamePortal) prev.image else null,
                            owner = if (isSamePortal) prev.owner else null,
                            latE6 = if (isSamePortal) prev.latE6 else null,
                            lngE6 = if (isSamePortal) prev.lngE6 else null,
                            address = if (isSamePortal) prev.address else null,
                            mods = if (isSamePortal) prev.mods else null
                        )
                        }
                    }
                } catch (e: Exception) {
                    println("setPortalStatus error: ${e.message}")
                }
            }
            "setPortalDetails" -> {
                try {
                    val guid = data.jsonObject["guid"]?.jsonPrimitive?.contentOrNull
                    if (guid.isNullOrBlank()) {
                        _portalStatus.value = PortalStatus()
                    } else {
                        val d = data.jsonObject["details"] as? JsonObject
                        if (d != null) {
                            val title = d["title"]?.jsonPrimitive?.contentOrNull?.ifBlank { null } ?: _portalStatus.value.title
                            val image = d["image"]?.jsonPrimitive?.contentOrNull?.ifBlank { null }
                            val owner = d["owner"]?.jsonPrimitive?.contentOrNull?.ifBlank { null }
                            val team = d["team"]?.jsonPrimitive?.contentOrNull ?: "N"
                            val level = d["level"]?.jsonPrimitive?.intOrNull ?: 0
                            val health = d["health"]?.jsonPrimitive?.intOrNull ?: 0
                            val latE6 = d["latE6"]?.jsonPrimitive?.longOrNull
                            val lngE6 = d["lngE6"]?.jsonPrimitive?.longOrNull
                            val address = d["address"]?.jsonPrimitive?.contentOrNull

                            // Parse resonators
                            val resElem = d["resonators"]
                            val resList = if (resElem is JsonArray) {
                                resElem.mapNotNull { rItem ->
                                    if (rItem is JsonObject) {
                                        val rLevel = rItem["level"]?.jsonPrimitive?.intOrNull ?: 0
                                        val rOwner = rItem["owner"]?.jsonPrimitive?.contentOrNull
                                        val rEnergy = rItem["energy"]?.jsonPrimitive?.intOrNull ?: 0
                                        val rMax = when (rLevel) {
                                            1 -> 1000; 2 -> 1500; 3 -> 2000; 4 -> 2500; 5 -> 3000; 6 -> 4000; 7 -> 5000; 8 -> 6000; else -> 1000
                                        }
                                        val pct = if (rMax > 0) ((rEnergy * 100) / rMax).coerceIn(0, 100) else 0
                                        Resonator(
                                            level = rLevel,
                                            levelColor = when (rLevel) {
                                                1 -> "#fece5a"; 2 -> "#ffa630"; 3 -> "#ff7315"; 4 -> "#e40000"; 5 -> "#fd2992"; 6 -> "#eb26cd"; 7 -> "#c124e0"; 8 -> "#9627f4"; else -> "#808080"
                                            },
                                            healthPct = pct,
                                            energy = rEnergy,
                                            owner = rOwner
                                        )
                                    } else null
                                }
                            } else _portalStatus.value.resonators

                            // Parse mods
                            val modsElem = d["mods"]
                            val modsList = if (modsElem is JsonArray) {
                                modsElem.map { mItem ->
                                    if (mItem is JsonObject) {
                                        PortalMod(
                                            name = mItem["name"]?.jsonPrimitive?.contentOrNull,
                                            rarity = mItem["rarity"]?.jsonPrimitive?.contentOrNull,
                                            owner = mItem["owner"]?.jsonPrimitive?.contentOrNull
                                        )
                                    } else PortalMod()
                                }
                            } else null

                            val rangeMeters = d["rangeMeters"]?.jsonPrimitive?.longOrNull
                            val linksIn = d["linksIn"]?.jsonPrimitive?.intOrNull ?: 0
                            val linksOut = d["linksOut"]?.jsonPrimitive?.intOrNull ?: 0
                            val fieldsCount = d["fieldsCount"]?.jsonPrimitive?.intOrNull ?: 0
                            val energy = d["energy"]?.jsonPrimitive?.longOrNull ?: 0L
                            val maxEnergy = d["maxEnergy"]?.jsonPrimitive?.longOrNull ?: 0L
                            val apGain = d["apGain"]?.jsonPrimitive?.longOrNull ?: 0L
                            val historyVisited = d["historyVisited"]?.jsonPrimitive?.booleanOrNull ?: false
                            val historyCaptured = d["historyCaptured"]?.jsonPrimitive?.booleanOrNull ?: false
                            val historyScoutControlled = d["historyScoutControlled"]?.jsonPrimitive?.booleanOrNull ?: false

                            val lpElem = d["linkedPortals"]
                            val linkedList = if (lpElem is JsonArray) {
                                lpElem.mapNotNull { lpItem ->
                                    if (lpItem is JsonObject) {
                                        LinkedPortal(
                                            guid = lpItem["guid"]?.jsonPrimitive?.contentOrNull ?: "",
                                            title = lpItem["title"]?.jsonPrimitive?.contentOrNull ?: "",
                                            lat = lpItem["lat"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                                            lng = lpItem["lng"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                                            isOutgoing = lpItem["isOutgoing"]?.jsonPrimitive?.booleanOrNull ?: true,
                                            team = lpItem["team"]?.jsonPrimitive?.contentOrNull ?: "N",
                                            level = lpItem["level"]?.jsonPrimitive?.intOrNull ?: 0,
                                            health = lpItem["health"]?.jsonPrimitive?.intOrNull ?: 100,
                                            distance = lpItem["distance"]?.jsonPrimitive?.longOrNull ?: 0L,
                                            image = lpItem["image"]?.jsonPrimitive?.contentOrNull?.ifBlank { null }
                                        )
                                    } else null
                                }
                            } else emptyList()

                            _portalStatus.value = _portalStatus.value.copy(
                                guid = guid,
                                title = title,
                                image = image ?: _portalStatus.value.image,
                                owner = owner ?: _portalStatus.value.owner,
                                team = team,
                                level = level,
                                isNeutral = team == "N",
                                health = health,
                                latE6 = latE6 ?: _portalStatus.value.latE6,
                                lngE6 = lngE6 ?: _portalStatus.value.lngE6,
                                address = address ?: _portalStatus.value.address,
                                rangeMeters = rangeMeters ?: _portalStatus.value.rangeMeters,
                                linksIn = linksIn,
                                linksOut = linksOut,
                                fieldsCount = fieldsCount,
                                energy = energy,
                                maxEnergy = maxEnergy,
                                apGain = apGain,
                                historyVisited = historyVisited,
                                historyCaptured = historyCaptured,
                                historyScoutControlled = historyScoutControlled,
                                resonators = resList,
                                mods = modsList ?: _portalStatus.value.mods,
                                linkedPortals = linkedList,
                                isLoading = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    println("setPortalDetails error: ${e.message}")
                }
            }
            "setChatMessages" -> {
                try {
                    val obj = data.jsonObject
                    val channel = (obj["channel"] as? JsonPrimitive)?.contentOrNull ?: "all"
                    val msgsElem = obj["messages"]
                    if (msgsElem is JsonArray) {
                        val parsedMsgs = msgsElem.mapNotNull { m ->
                            if (m is JsonObject) {
                                val pObj = m["portal"] as? JsonObject
                                val chatPortal = if (pObj != null) {
                                    ChatPortal(
                                        name = (pObj["name"] as? JsonPrimitive)?.contentOrNull ?: "",
                                        lat = (pObj["lat"] as? JsonPrimitive)?.doubleOrNull ?: 0.0,
                                        lng = (pObj["lng"] as? JsonPrimitive)?.doubleOrNull ?: 0.0
                                    )
                                } else null

                                ChatMessage(
                                    id = (m["id"] as? JsonPrimitive)?.contentOrNull ?: "",
                                    time = (m["time"] as? JsonPrimitive)?.longOrNull ?: 0L,
                                    formattedTime = (m["formattedTime"] as? JsonPrimitive)?.contentOrNull ?: "",
                                    sender = (m["sender"] as? JsonPrimitive)?.contentOrNull ?: "",
                                    team = (m["team"] as? JsonPrimitive)?.contentOrNull ?: "NEUTRAL",
                                    text = (m["text"] as? JsonPrimitive)?.contentOrNull ?: "",
                                    isSecure = (m["isSecure"] as? JsonPrimitive)?.booleanOrNull ?: false,
                                    portal = chatPortal
                                )
                            } else null
                        }
                        val current = _chatMessages.value.toMutableMap()
                        current[channel] = parsedMsgs
                        _chatMessages.value = current
                        if (_isPlayerTrackerEnabled.value) {
                            updatePlayerTrails(current)
                        }
                    }
                } catch (e: Exception) {
                    println("setChatMessages error: ${e.message}")
                }
            }
            "setMapStatus" -> {
                try {
                    val obj = data.jsonObject
                    val portalLevelsObj = obj["portalLevels"] as? JsonObject
                    val mapStatusObj = obj["mapStatus"] as? JsonObject
                    val requestsObj = obj["requests"] as? JsonObject

                    val pInfo = if (portalLevelsObj != null) {
                        PortalLevelsInfo(
                            hasPortals = portalLevelsObj["hasPortals"]?.jsonPrimitive?.booleanOrNull ?: false,
                            minLinkLength = portalLevelsObj["minLinkLength"]?.jsonPrimitive?.longOrNull ?: 0L,
                            formattedLength = portalLevelsObj["formattedLength"]?.jsonPrimitive?.contentOrNull ?: "0m"
                        )
                    } else PortalLevelsInfo()

                    val mInfo = if (mapStatusObj != null) {
                        MapStatusDetails(
                            short = mapStatusObj["short"]?.jsonPrimitive?.contentOrNull,
                            long = mapStatusObj["long"]?.jsonPrimitive?.contentOrNull,
                            progress = mapStatusObj["progress"]?.jsonPrimitive?.floatOrNull ?: 1f,
                            progressPercent = mapStatusObj["progressPercent"]?.jsonPrimitive?.intOrNull
                        )
                    } else MapStatusDetails()

                    val rInfo = if (requestsObj != null) {
                        MapRequestsInfo(
                            active = requestsObj["active"]?.jsonPrimitive?.intOrNull ?: 0,
                            failed = requestsObj["failed"]?.jsonPrimitive?.intOrNull ?: 0,
                            hasActive = requestsObj["hasActive"]?.jsonPrimitive?.booleanOrNull ?: false,
                            hasFailed = requestsObj["hasFailed"]?.jsonPrimitive?.booleanOrNull ?: false
                        )
                    } else MapRequestsInfo()

                    _mapStatus.value = MapStatus(pInfo, mInfo, rInfo)
                } catch (e: Exception) {
                    println("setMapStatus error: ${e.message}")
                }
            }
            "setProgress" -> {
                val prog = data.jsonObject["progress"]?.jsonPrimitive?.floatOrNull ?: 1f
                _progress.value = prog
            }
            "saveFile" -> {
                val obj = data.jsonObject
                val filename = obj["filename"]?.jsonPrimitive?.contentOrNull ?: "iitc_export.txt"
                val content = obj["content"]?.jsonPrimitive?.contentOrNull ?: ""
                val dataType = obj["dataType"]?.jsonPrimitive?.contentOrNull ?: "text/plain"
                PlatformUtils.shareFile(filename, content.encodeToByteArray(), dataType)
            }
            "dialogOpened" -> {
                val id = data.jsonObject["id"]?.jsonPrimitive?.contentOrNull ?: return
                val open = data.jsonObject["open"]?.jsonPrimitive?.booleanOrNull ?: true
                if (open) {
                    if (!_dialogStack.value.contains(id)) {
                        _dialogStack.value = _dialogStack.value + id
                    }
                } else {
                    _dialogStack.value = _dialogStack.value.filter { it != id }
                }
            }
            "dialogFocused" -> {
                val id = data.jsonObject["id"]?.jsonPrimitive?.contentOrNull ?: return
                _dialogStack.value = _dialogStack.value.filter { it != id } + id
            }
            "openDemo" -> {
                loadUrl(UrlConfig.DEMO_INTEL_MAP)
            }
            "spinnerEnabled" -> {}
            "gmBridgeRequest" -> {
                val raw = data.jsonObject["data"]?.jsonPrimitive?.content ?: data.toString()
                manager.handleGmBridgeRequest(raw)
            }
            "setEntities" -> {
                try {
                    val raw = when {
                        data is JsonObject && data.containsKey("data") -> data["data"]?.jsonPrimitive?.content ?: ""
                        data is JsonObject -> data.toString()
                        data is JsonPrimitive -> data.content
                        else -> ""
                    }
                    if (raw.isNotBlank()) {
                        parseNativeEntities(raw)
                    }
                } catch (e: Exception) {
                    println("setEntities error: ${e.message}")
                }
            }
        }
    }

    private fun parseNativeEntities(raw: String) {
        scope.launch(Dispatchers.Default) {
            try {
                val root = json.parseToJsonElement(raw).jsonObject
                val portalsJson = root["portals"]?.jsonArray
                val linksJson = root["links"]?.jsonArray
                val fieldsJson = root["fields"]?.jsonArray

                val portalsList = portalsJson?.mapNotNull { item ->
                    if (item is JsonArray && item.size >= 6) {
                        val resCount = if (item.size >= 7) item[6].jsonPrimitive.intOrNull ?: 8 else 8
                        val title = if (item.size >= 8) item[7].jsonPrimitive.contentOrNull ?: "" else ""
                        NativePortal(
                            guid = item[0].jsonPrimitive.content,
                            lat = item[1].jsonPrimitive.double,
                            lng = item[2].jsonPrimitive.double,
                            team = item[3].jsonPrimitive.int,
                            level = item[4].jsonPrimitive.int,
                            health = item[5].jsonPrimitive.int,
                            resCount = resCount,
                            title = title
                        )
                    } else null
                } ?: emptyList()

                val linksList = linksJson?.mapNotNull { item ->
                    if (item is JsonArray && item.size >= 6) {
                        NativeLink(
                            guid = item[0].jsonPrimitive.content,
                            team = item[1].jsonPrimitive.int,
                            oLat = item[2].jsonPrimitive.double,
                            oLng = item[3].jsonPrimitive.double,
                            dLat = item[4].jsonPrimitive.double,
                            dLng = item[5].jsonPrimitive.double
                        )
                    } else null
                } ?: emptyList()

                val fieldsList = fieldsJson?.mapNotNull { item ->
                    if (item is JsonArray && item.size >= 3) {
                        val guid = item[0].jsonPrimitive.content
                        val team = item[1].jsonPrimitive.int
                        val ptsArr = item[2].jsonArray
                        val pts = ptsArr.mapNotNull { pt ->
                            if (pt is JsonArray && pt.size >= 2) {
                                listOf(pt[0].jsonPrimitive.double, pt[1].jsonPrimitive.double)
                            } else null
                        }
                        NativeField(guid = guid, team = team, points = pts)
                    } else null
                } ?: emptyList()

                _nativeMapPortals.value = portalsList
                _nativeMapEntities.value = NativeMapEntities(
                    portals = portalsList,
                    links = linksList,
                    fields = fieldsList
                )
            } catch (e: Exception) {
                println("parseNativeEntities error: ${e.message}")
            }
        }
    }
}
