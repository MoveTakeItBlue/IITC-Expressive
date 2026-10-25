package org.exarhteam.iitcprime.viewmodel

import kotlinx.coroutines.flow.*
import org.exarhteam.iitcprime.platform.PlatformUtils
import org.exarhteam.iitcprime.storage.AppStorage

class SettingsViewModel(
    private val storage: AppStorage,
    private val mainViewModel: MainViewModel
) {
    private val _desktopMode = MutableStateFlow(storage.getBoolean("desktop_mode", false))
    val desktopMode: StateFlow<Boolean> = _desktopMode.asStateFlow()

    private val _zoomControl = MutableStateFlow(storage.getBoolean("zoom_control", false))
    val zoomControl: StateFlow<Boolean> = _zoomControl.asStateFlow()

    private val _fakeUserAgent = MutableStateFlow(storage.getBoolean("fake_user_agent", false))
    val fakeUserAgent: StateFlow<Boolean> = _fakeUserAgent.asStateFlow()

    private val _persistentZoom = MutableStateFlow(storage.getBoolean("persistent_zoom", false))
    val persistentZoom: StateFlow<Boolean> = _persistentZoom.asStateFlow()

    private val _showLocation = MutableStateFlow(storage.getBoolean("show_location", false))
    val showLocation: StateFlow<Boolean> = _showLocation.asStateFlow()

    val currentChannel: StateFlow<org.exarhteam.iitcprime.model.Channel> = mainViewModel.manager.currentChannel

    val enabledPluginsCount: StateFlow<Int> = mainViewModel.manager.plugins
        .map { it.values.count { p -> p.isEnabled } }
        .stateIn(mainViewModel.scope, SharingStarted.Lazily, 0)

    fun setDesktopMode(enabled: Boolean) {
        _desktopMode.value = enabled
        storage.setBoolean("desktop_mode", enabled)
        mainViewModel.loadUrl(mainViewModel.currentUrl.value)
    }

    fun setZoomControl(enabled: Boolean) {
        _zoomControl.value = enabled
        storage.setBoolean("zoom_control", enabled)
        mainViewModel.reloadWebView(clearCache = false)
    }

    fun setFakeUserAgent(enabled: Boolean) {
        _fakeUserAgent.value = enabled
        storage.setBoolean("fake_user_agent", enabled)
        mainViewModel.reloadWebView(clearCache = false)
    }

    fun setPersistentZoom(enabled: Boolean) {
        _persistentZoom.value = enabled
        storage.setBoolean("persistent_zoom", enabled)
    }

    fun setShowLocation(enabled: Boolean) {
        _showLocation.value = enabled
        storage.setBoolean("show_location", enabled)
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
