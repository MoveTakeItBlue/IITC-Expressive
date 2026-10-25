package org.exarhteam.iitcprime.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.exarhteam.iitcprime.manager.BackupManager
import org.exarhteam.iitcprime.ui.chat.ChatSheet
import org.exarhteam.iitcprime.ui.common.AlertDialog
import org.exarhteam.iitcprime.ui.common.ConfirmDialog
import org.exarhteam.iitcprime.ui.common.LocationShareDialog
import org.exarhteam.iitcprime.ui.debug.DebugConsole
import org.exarhteam.iitcprime.ui.layers.LayersSheet
import org.exarhteam.iitcprime.ui.menu.FabMenu
import org.exarhteam.iitcprime.ui.portal.PortalDetailsSheet
import org.exarhteam.iitcprime.ui.settings.*
import org.exarhteam.iitcprime.viewmodel.*

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
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }

            if (!isDebugActive) {
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
                        bottomOffset = if (portalStatus.hasSelected) 96.dp else 16.dp,
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
