package org.exarhteam.iitcprime.ui.settings

import androidx.compose.runtime.*
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.ui.common.ConfirmDialog
import org.exarhteam.iitcprime.viewmodel.SettingsViewModel

@Composable
fun SettingsView(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    onNavigateToUpdateChannel: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToLicenses: () -> Unit
) {
    val desktopMode by viewModel.desktopMode.collectAsState()
    val zoomControl by viewModel.zoomControl.collectAsState()
    val fakeUserAgent by viewModel.fakeUserAgent.collectAsState()
    val persistentZoom by viewModel.persistentZoom.collectAsState()
    val showLocation by viewModel.showLocation.collectAsState()
    val currentChannel by viewModel.currentChannel.collectAsState()

    var showClearCookiesConfirm by remember { mutableStateOf(false) }

    if (showClearCookiesConfirm) {
        ConfirmDialog(
            title = L("settings.clear_cookies.title"),
            message = L("settings.clear_cookies.confirm"),
            onConfirm = {
                viewModel.clearCookies()
                showClearCookiesConfirm = false
            },
            onCancel = {
                showClearCookiesConfirm = false
            }
        )
    }

    SettingsBase(
        title = L("settings.title"),
        onBack = onBack
    ) {
        // IITC Management section
        SettingsSection(title = L("settings.section.iitc_management"))
        NavSettingsItem(
            title = L("settings.plugins.title"),
            description = L("settings.plugins.description"),
            isFirst = true,
            onClick = onNavigateToPlugins
        )
        NavSettingsItem(
            title = L("settings.update_channel.title"),
            description = L("update_channel.channel.${currentChannel.name.lowercase()}"),
            isLast = true,
            onClick = onNavigateToUpdateChannel
        )

        // Map settings section
        SettingsSection(title = L("settings.section.map"))
        SwitchSettingsItem(
            title = L("settings.location.title"),
            description = L("settings.location.description"),
            checked = showLocation,
            onCheckedChange = { viewModel.setShowLocation(it) },
            isFirst = true
        )
        SwitchSettingsItem(
            title = L("settings.persistent_zoom.title"),
            description = L("settings.persistent_zoom.description"),
            checked = persistentZoom,
            onCheckedChange = { viewModel.setPersistentZoom(it) },
            isLast = true
        )

        // UI settings section
        SettingsSection(title = L("settings.section.interface"))
        SwitchSettingsItem(
            title = L("settings.desktop_mode.title"),
            description = L("settings.desktop_mode.description"),
            checked = desktopMode,
            onCheckedChange = { viewModel.setDesktopMode(it) },
            isFirst = true
        )
        SwitchSettingsItem(
            title = L("settings.zoom_controls.title"),
            description = L("settings.zoom_controls.description"),
            checked = zoomControl,
            onCheckedChange = { viewModel.setZoomControl(it) }
        )
        SwitchSettingsItem(
            title = L("settings.fake_ua.title"),
            description = L("settings.fake_ua.description"),
            checked = fakeUserAgent,
            onCheckedChange = { viewModel.setFakeUserAgent(it) },
            isLast = true
        )

        // Data Management section
        SettingsSection(title = L("settings.section.data"))
        NavSettingsItem(
            title = L("settings.backup.title"),
            description = L("settings.backup.description"),
            isFirst = true,
            onClick = onNavigateToBackup
        )
        ActionSettingsItem(
            title = L("settings.clear_cookies.title"),
            description = L("settings.clear_cookies.description"),
            isLast = true,
            onClick = { showClearCookiesConfirm = true }
        )

        // About section
        SettingsSection(title = L("settings.section.information"))
        NavSettingsItem(
            title = L("settings.about.title"),
            description = L("settings.about.description"),
            isFirst = true,
            onClick = onNavigateToAbout
        )
        NavSettingsItem(
            title = L("settings.licenses.title"),
            description = L("settings.licenses.description"),
            isLast = true,
            onClick = onNavigateToLicenses
        )
    }
}
