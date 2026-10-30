package io.github.movetakeitblue.iitcexpressive.viewmodel

import kotlinx.coroutines.flow.*
import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils
import io.github.movetakeitblue.iitcexpressive.storage.AppStorage

class SettingsViewModel(
    private val storage: AppStorage,
    private val mainViewModel: MainViewModel
) {
    private val _desktopMode = MutableStateFlow(storage.getBoolean("desktop_mode", false))
    val desktopMode: StateFlow<Boolean> = _desktopMode.asStateFlow()

    val zoomControl: StateFlow<Boolean> = mainViewModel.isZoomControlEnabled

    private val _persistentZoom = MutableStateFlow(storage.getBoolean("persistent_zoom", false))
    val persistentZoom: StateFlow<Boolean> = _persistentZoom.asStateFlow()

    val showLocation: StateFlow<Boolean> = mainViewModel.showLocation

    val fullscreenMap: StateFlow<Boolean> = mainViewModel.isFullscreenMap

    fun setFullscreenMap(enabled: Boolean) {
        mainViewModel.setFullscreenMap(enabled)
    }

    val nativeMapEnabled: StateFlow<Boolean> = mainViewModel.isNativeMapEnabled

    fun setNativeMapEnabled(enabled: Boolean) {
        mainViewModel.setNativeMapEnabled(enabled)
    }

    val mapTextColor: StateFlow<String> = mainViewModel.mapTextColor
    fun setMapTextColor(color: String) { mainViewModel.setMapTextColor(color) }

    val portalNameFontSize: StateFlow<Float> = mainViewModel.portalNameFontSize
    fun setPortalNameFontSize(size: Float) { mainViewModel.setPortalNameFontSize(size) }

    val agentNameFontSize: StateFlow<Float> = mainViewModel.agentNameFontSize
    fun setAgentNameFontSize(size: Float) { mainViewModel.setAgentNameFontSize(size) }

    val mapFontFamily: StateFlow<String> = mainViewModel.mapFontFamily
    fun setMapFontFamily(family: String) { mainViewModel.setMapFontFamily(family) }

    val mapFontBold: StateFlow<Boolean> = mainViewModel.mapFontBold
    fun setMapFontBold(bold: Boolean) { mainViewModel.setMapFontBold(bold) }

    val mapTextHalo: StateFlow<Boolean> = mainViewModel.mapTextHalo
    fun setMapTextHalo(enabled: Boolean) { mainViewModel.setMapTextHalo(enabled) }

    val playerIconScale: StateFlow<Float> = mainViewModel.playerIconScale
    fun setPlayerIconScale(scale: Float) { mainViewModel.setPlayerIconScale(scale) }

    val currentChannel: StateFlow<io.github.movetakeitblue.iitcexpressive.model.Channel> = mainViewModel.manager.currentChannel

    val enabledPluginsCount: StateFlow<Int> = mainViewModel.manager.plugins
        .map { it.values.count { p -> p.isEnabled } }
        .stateIn(mainViewModel.scope, SharingStarted.Lazily, 0)

    fun setDesktopMode(enabled: Boolean) {
        _desktopMode.value = enabled
        storage.setBoolean("desktop_mode", enabled)
        mainViewModel.setDesktopMode(enabled)
    }

    fun setZoomControl(enabled: Boolean) {
        mainViewModel.setZoomControl(enabled)
    }

    fun setPersistentZoom(enabled: Boolean) {
        _persistentZoom.value = enabled
        storage.setBoolean("persistent_zoom", enabled)
    }

    fun setShowLocation(enabled: Boolean) {
        mainViewModel.setShowLocation(enabled)
    }

    fun clearCookies(onResult: (Boolean) -> Unit = {}) {
        PlatformUtils.clearCookies { success ->
            if (success) {
                mainViewModel.reloadWebView(clearCache = true)
            }
            onResult(success)
        }
    }
}
