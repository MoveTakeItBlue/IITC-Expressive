package org.exarhteam.iitcprime.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.exarhteam.iitcprime.manager.BackupManager
import org.exarhteam.iitcprime.theme.Accent
import org.exarhteam.iitcprime.theme.AccentTeal
import org.exarhteam.iitcprime.theme.SurfaceDark
import org.exarhteam.iitcprime.ui.common.AlertDialog
import org.exarhteam.iitcprime.ui.common.ConfirmDialog
import org.exarhteam.iitcprime.ui.common.LocationShareDialog
import org.exarhteam.iitcprime.ui.debug.DebugConsole
import org.exarhteam.iitcprime.ui.panel.BottomSheetPanel
import org.exarhteam.iitcprime.ui.panel.MapStateBar
import org.exarhteam.iitcprime.ui.panel.RestorePanelButton
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

    // ViewModels for sub-screens
    val settingsViewModel = remember(viewModel) { SettingsViewModel(viewModel.storage, viewModel) }
    val pluginsViewModel = remember(viewModel) { PluginsViewModel(viewModel.manager) }
    val updateChannelViewModel = remember(viewModel) { UpdateChannelViewModel(viewModel.manager, viewModel.storage) }
    val backupViewModel = remember(viewModel) { BackupViewModel(BackupManager(viewModel.storage, viewModel.manager)) }

    // Check clipboard periodically or on start
    LaunchedEffect(Unit) {
        viewModel.checkClipboard()
    }

    Box(modifier = modifier.fillMaxSize().background(SurfaceDark)) {
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
            else -> {
                // Main Map & IITC UI
                Box(modifier = Modifier.fillMaxSize()) {
                    // WebView
                    AppWebView(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )

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
                            color = AccentTeal,
                            trackColor = Color.Transparent
                        )
                    }

                    // Bottom sheet panel
                    if (!isDebugActive) {
                        BottomSheetPanel(
                            viewModel = viewModel,
                            onNavigateToSettings = { viewModel.navigateTo("settings") },
                            onNavigateToPlugins = { viewModel.navigateTo("plugins") },
                            modifier = Modifier.fillMaxSize()
                        )

                        // MapStateBar overlay
                        MapStateBar(
                            viewModel = viewModel,
                            modifier = Modifier.align(Alignment.BottomStart)
                        )

                        // Restore panel button when hidden
                        if (panelPosition == PanelPosition.HIDDEN) {
                            RestorePanelButton(
                                onRestore = { viewModel.restorePanel() }
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
