package io.github.movetakeitblue.iitcexpressive.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.movetakeitblue.iitcexpressive.manager.BackupManager
import io.github.movetakeitblue.iitcexpressive.plugin.core.NativePluginRegistry
import io.github.movetakeitblue.iitcexpressive.plugin.ui.drawtools.DrawToolbar
import io.github.movetakeitblue.iitcexpressive.ui.chat.ChatSheet
import io.github.movetakeitblue.iitcexpressive.ui.common.AlertDialog
import io.github.movetakeitblue.iitcexpressive.ui.common.ConfirmDialog
import io.github.movetakeitblue.iitcexpressive.ui.common.ExpressiveLinearProgressIndicator
import io.github.movetakeitblue.iitcexpressive.ui.common.LocationShareDialog
import io.github.movetakeitblue.iitcexpressive.ui.common.PlayerTrailDialog
import io.github.movetakeitblue.iitcexpressive.ui.debug.DebugConsole
import io.github.movetakeitblue.iitcexpressive.ui.layers.LayersSheet
import io.github.movetakeitblue.iitcexpressive.ui.map.NativeMapView
import io.github.movetakeitblue.iitcexpressive.ui.menu.FabMenu
import io.github.movetakeitblue.iitcexpressive.ui.portal.PortalDetailsSheet
import io.github.movetakeitblue.iitcexpressive.ui.portal.PortalSheetDefaults
import io.github.movetakeitblue.iitcexpressive.ui.settings.*
import io.github.movetakeitblue.iitcexpressive.viewmodel.*

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val isDebugActive by viewModel.isDebugActive.collectAsState()
    val panelPosition by viewModel.panelPosition.collectAsState()
    val currentDialog by viewModel.currentDialog.collectAsState()
    val isLayersSheetOpen by viewModel.isLayersSheetOpen.collectAsState()
    val isChatSheetOpen by viewModel.isChatSheetOpen.collectAsState()
    val isFabMenuExpanded by viewModel.isFabMenuExpanded.collectAsState()
    val portalStatus by viewModel.portalStatus.collectAsState()
    val isPortalDetailsExpanded by viewModel.isPortalDetailsExpanded.collectAsState()
    val mapStatus by viewModel.mapStatus.collectAsState()
    val isFullscreenMap by viewModel.isFullscreenMap.collectAsState()
    val isMapDark by viewModel.isMapDark.collectAsState()

    // Adapt status bar icons/text contrast dynamically based on current screen & fullscreen map mode:
    // When in fullscreen map mode on main screen, contrast matches the map brightness:
    // - Dark map -> White status bar icons (isDarkContent = false)
    // - Light map -> Dark/black status bar icons (isDarkContent = true)
    // Otherwise, contrast matches the theme's surfaceContainer.
    val statusBarColor = MaterialTheme.colorScheme.surfaceContainer
    val isStatusBarLight = (statusBarColor.red * 0.299f + statusBarColor.green * 0.587f + statusBarColor.blue * 0.114f) >= 0.5f
    val isEffectiveStatusBarDarkContent = remember(currentScreen, isFullscreenMap, isMapDark, isStatusBarLight) {
        if (currentScreen == "main" && isFullscreenMap) {
            !isMapDark
        } else {
            isStatusBarLight
        }
    }
    LaunchedEffect(isEffectiveStatusBarDarkContent) {
        PlatformUtils.setStatusBarContrast(isEffectiveStatusBarDarkContent)
    }

    // ViewModels for sub-screens
    val settingsViewModel = remember(viewModel) { SettingsViewModel(viewModel.storage, viewModel) }
    val pluginsViewModel = remember(viewModel) { PluginsViewModel(viewModel.manager) }
    val updateChannelViewModel = remember(viewModel) { UpdateChannelViewModel(viewModel.manager, viewModel.storage) }
    val backupViewModel = remember(viewModel) { BackupViewModel(BackupManager(viewModel.storage, viewModel.manager)) }

    // Check clipboard periodically or on start
    LaunchedEffect(Unit) {
        viewModel.checkClipboard()
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Map and main overlay container.
        // 全面屏显示地图：开启后顶部不留白，地图直接铺满到手机屏幕顶部状态栏区域；
        // 未开启时，顶部避开状态栏，保持与状态栏的分隔。
        // 底部避开导航栏，确保地图下边缘与导航栏相邻。
        val mapContainerModifier = if (isFullscreenMap) {
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
        } else {
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
        }

        Box(modifier = mapContainerModifier) {
            // AppWebView is always mounted so map state is preserved when navigating to settings/plugins
            AppWebView(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )

            // Native GPU MapView: renders when IITC is loaded and native map is enabled
            val isNativeMapEnabled by viewModel.isNativeMapEnabled.collectAsState()
            val isIitcLoaded by viewModel.isIitcLoaded.collectAsState()

            if (isNativeMapEnabled && isIitcLoaded) {
                NativeMapView(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Main Map UI overlay
            if (currentScreen == "main") {
                val isMapSyncing = progress < 1f || mapStatus.requests.hasActive

                val statusBarTopPadding = if (isFullscreenMap) {
                    WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                } else {
                    0.dp
                }

                // Top Expressive Floating Pill Indicator
                AnimatedVisibility(
                    visible = isMapSyncing,
                    enter = fadeIn() + slideInVertically { -it },
                    exit = fadeOut() + slideOutVertically { -it },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = statusBarTopPadding + 10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                        tonalElevation = 6.dp,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (progress in 0.01f..0.99f) {
                                ExpressiveLinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.width(72.dp).height(6.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                            } else {
                                ExpressiveLinearProgressIndicator(
                                    modifier = Modifier.width(72.dp).height(6.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                            }

                            Text(
                                text = if (mapStatus.requests.hasActive) {
                                    if (mapStatus.requests.active > 0) "SYNCING (${mapStatus.requests.active})" else "SYNCING"
                                } else if (progress < 1f) {
                                    "${(progress * 100).toInt()}%"
                                } else {
                                    "SYNCING"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (!isDebugActive) {
                    // Portal 详情 sheet 收起时占用的高度（含导航栏 inset）。
                    // FAB 必须让出这块空间，否则会被 sheet 压住；这里与 sheet 内部用的是同一套计算。
                    val sheetPeekHeight = PortalSheetDefaults.PeekHeight +
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

                    // Portal Details Sheet (Collapsed peek bar or expanded full details)
                    PortalDetailsSheet(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )

                    // FAB Menu for quick actions and settings
                    AnimatedVisibility(
                        visible = !isPortalDetailsExpanded,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        FabMenu(
                            isExpanded = isFabMenuExpanded,
                            onToggle = { viewModel.toggleFabMenu() },
                            onNavigateToSettings = {
                                viewModel.closeFabMenu()
                                viewModel.navigateTo("settings")
                            },
                            onNavigateToPlugins = {
                                viewModel.closeFabMenu()
                                viewModel.navigateTo("plugins")
                            },
                            onOpenLayers = { viewModel.openLayersSheet() },
                            onOpenChat = { viewModel.openChatSheet() },
                            onOpenBookmarks = { viewModel.openPluginSheet("bookmarks") },
                            onOpenPortalList = { viewModel.openPluginSheet("portals-list") },
                            onOpenDrawTools = { viewModel.openPluginSheet("draw-tools") },
                            onLocate = {
                                viewModel.closeFabMenu()
                                viewModel.triggerUserLocate()
                            },
                            onDebug = {
                                viewModel.closeFabMenu()
                                viewModel.toggleDebugMode()
                            },
                            onReload = {
                                viewModel.closeFabMenu()
                                viewModel.reloadWebView()
                            },
                            // portal 被选中后，FAB 跟随 sheet 一起向上让位
                            bottomOffset = if (portalStatus.hasSelected) {
                                sheetPeekHeight + PortalSheetDefaults.FabGap
                            } else {
                                16.dp
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    // Floating DrawToolbar overlay when drawing mode is active
                    val isDrawingModeActive by viewModel.isDrawingModeActive.collectAsState()
                    if (isDrawingModeActive) {
                        DrawToolbar(
                            host = viewModel.pluginHost,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = if (portalStatus.hasSelected) sheetPeekHeight + 16.dp else 24.dp)
                        )
                    }
                }

                // Debug Console overlay
                if (isDebugActive) {
                    DebugConsole(
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (isFullscreenMap) Modifier.statusBarsPadding() else Modifier)
                    )
                }
            }
        }

        // 状态栏：全面屏模式下使用优雅的渐变护罩保护文字可读性；普通模式下使用 M3E 实色表面覆盖
        if (currentScreen == "main" && isFullscreenMap) {
            val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val scrimColor = if (isMapDark) Color.Black else Color.White
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarHeight + 16.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                scrimColor.copy(alpha = 0.50f),
                                scrimColor.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        )
                    )
            )
        } else if (!isFullscreenMap) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .align(Alignment.TopCenter)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                verticalArrangement = Arrangement.Bottom
            ) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            }
        }

        // 导航栏：同样用实色表面填充，让地图下边缘正好与导航栏相邻
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .align(Alignment.BottomCenter)
                .background(MaterialTheme.colorScheme.surfaceContainer)
        )

        // Sub-screens layered on top of map
        when (currentScreen) {
            "settings" -> {
                SettingsView(
                    viewModel = settingsViewModel,
                    onBack = { viewModel.navigateTo("main") },
                    onNavigateToPlugins = { viewModel.navigateTo("plugins") },
                    onNavigateToUpdateChannel = { viewModel.navigateTo("update_channel") },
                    onNavigateToBackup = { viewModel.navigateTo("backup") },
                    onNavigateToAbout = { viewModel.navigateTo("about") },
                    onNavigateToLicenses = { viewModel.navigateTo("licenses") }
                )
            }
            "plugins" -> {
                PluginsView(
                    viewModel = pluginsViewModel,
                    onBack = { viewModel.navigateTo("settings") }
                )
            }
            "update_channel" -> {
                UpdateChannelView(
                    viewModel = updateChannelViewModel,
                    onBack = { viewModel.navigateTo("settings") }
                )
            }
            "backup" -> {
                BackupView(
                    viewModel = backupViewModel,
                    onBack = { viewModel.navigateTo("settings") }
                )
            }
            "about" -> {
                AboutView(
                    onBack = { viewModel.navigateTo("settings") }
                )
            }
            "licenses" -> {
                LicensesView(
                    onBack = { viewModel.navigateTo("settings") }
                )
            }
        }

        // Secondary Bottom Sheets
        if (isLayersSheetOpen) {
            LayersSheet(
                viewModel = viewModel,
                onDismiss = { viewModel.closeLayersSheet() }
            )
        }

        if (isChatSheetOpen) {
            ChatSheet(
                viewModel = viewModel,
                onDismiss = { viewModel.closeChatSheet() }
            )
        }

        // Native M3E Core Plugin BottomSheets
        val activePluginSheet by viewModel.activePluginSheet.collectAsState()
        activePluginSheet?.let { sheetId ->
            val plugin = NativePluginRegistry.getById(sheetId)
            plugin?.SheetContent(
                host = viewModel.pluginHost,
                onDismiss = { viewModel.closePluginSheet() }
            )
        }

        // Global Dialogs Layer
        when (val dialog = currentDialog) {
            is AppDialog.Alert -> {
                AlertDialog(
                    title = dialog.title,
                    message = dialog.message,
                    onDismiss = {
                        dialog.onDismiss()
                        viewModel.setDialog(null)
                    }
                )
            }
            is AppDialog.Confirm -> {
                ConfirmDialog(
                    title = dialog.title,
                    message = dialog.message,
                    onConfirm = {
                        dialog.onConfirm()
                        viewModel.setDialog(null)
                    },
                    onCancel = {
                        dialog.onCancel()
                        viewModel.setDialog(null)
                    }
                )
            }
            is AppDialog.LocationShare -> {
                LocationShareDialog(
                    dialog = dialog,
                    onDismiss = { viewModel.setDialog(null) }
                )
            }
            is AppDialog.AddPlugin -> {
                AddPluginSheet(
                    initialUrl = dialog.initialUrl,
                    onLoadUrl = { url ->
                        viewModel.setDialog(null)
                        viewModel.navigateTo("plugins")
                        pluginsViewModel.loadPluginFromUrl(url)
                    },
                    onChooseFile = {
                        viewModel.setDialog(null)
                        viewModel.navigateTo("plugins")
                        pluginsViewModel.loadPluginFromFile()
                    },
                    onDismiss = { viewModel.setDialog(null) }
                )
            }
            is AppDialog.PluginInfo -> {
                PluginInfoSheet(
                    plugin = dialog.plugin,
                    onDismiss = { viewModel.setDialog(null) }
                )
            }
            is AppDialog.PlayerTrailInfo -> {
                PlayerTrailDialog(
                    trail = dialog.trail,
                    onPortalClick = { lat, lng ->
                        viewModel.zoomToLocation(lat, lng, 17.0)
                        viewModel.setDialog(null)
                    },
                    onDismiss = { viewModel.setDialog(null) }
                )
            }
            null -> {}
        }
    }
}
