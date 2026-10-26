package io.github.movetakeitblue.iitcexpressive.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.movetakeitblue.iitcexpressive.manager.BackupManager
import io.github.movetakeitblue.iitcexpressive.ui.chat.ChatSheet
import io.github.movetakeitblue.iitcexpressive.ui.common.AlertDialog
import io.github.movetakeitblue.iitcexpressive.ui.common.ConfirmDialog
import io.github.movetakeitblue.iitcexpressive.ui.common.ExpressiveLinearProgressIndicator
import io.github.movetakeitblue.iitcexpressive.ui.common.LocationShareDialog
import io.github.movetakeitblue.iitcexpressive.ui.debug.DebugConsole
import io.github.movetakeitblue.iitcexpressive.ui.layers.LayersSheet
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
        // 地图安全区：顶部避开状态栏，底部避开导航栏，正好占满「状态栏下沿 → 导航栏上沿」，
        // 因此地图下边缘与导航栏相邻、上边缘与状态栏分隔，地图内容不会画到系统栏上。
        // 注意：windowInsetsPadding 会消费 inset，内部子组件再调用 navigationBarsPadding() 会自动变 0，不会二次留白。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // AppWebView is always mounted so map state is preserved when navigating to settings/plugins
            AppWebView(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )

            // Main Map UI overlay
            if (currentScreen == "main") {
                // Top progress indicator
                AnimatedVisibility(
                    visible = progress < 1f,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                ) {
                    ExpressiveLinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                    )
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
                }

                // Debug Console overlay
                if (isDebugActive) {
                    DebugConsole(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // 状态栏：M3E 实色表面覆盖，底部带一条细分割线，让地图与状态栏有明确的视觉分隔
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
            null -> {}
        }
    }
}
