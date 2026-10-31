package io.github.movetakeitblue.iitcexpressive.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.movetakeitblue.iitcexpressive.i18n.L
import io.github.movetakeitblue.iitcexpressive.ui.common.ConfirmDialog
import io.github.movetakeitblue.iitcexpressive.viewmodel.SettingsViewModel

@Composable
fun SettingsView(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    onNavigateToUpdateChannel: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToLicenses: () -> Unit
) {
    val persistentZoom by viewModel.persistentZoom.collectAsState()
    val showLocation by viewModel.showLocation.collectAsState()
    val nativeMapEnabled by viewModel.nativeMapEnabled.collectAsState()
    val currentChannel by viewModel.currentChannel.collectAsState()
    val enabledPluginsCount by viewModel.enabledPluginsCount.collectAsState()

    var showClearCookiesConfirm by remember { mutableStateOf(false) }

    if (showClearCookiesConfirm) {
        ConfirmDialog(
            title = L("settings.clear_cookies.title"),
            message = L("settings.clear_cookies.dialog.message"),
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
        // Appearance section
        SettingsSection(title = L("settings.section.appearance"))
        NavSettingsItem(
            title = L("settings.appearance.title"),
            description = L("settings.appearance.description"),
            isFirst = true,
            isLast = true,
            onClick = onNavigateToAppearance
        )

        Spacer(modifier = Modifier.height(16.dp))

        // IITC Management section
        SettingsSection(title = L("settings.section.iitc_management"))
        NavSettingsItem(
            title = L("settings.plugins.title"),
            description = L("settings.plugins.description", enabledPluginsCount),
            isFirst = true,
            onClick = onNavigateToPlugins
        )
        NavSettingsItem(
            title = L("settings.update_channel.title"),
            description = L("settings.update_channel.description", L("update_channel.${currentChannel.key}")),
            isLast = true,
            onClick = onNavigateToUpdateChannel
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Map & Location settings section
        SettingsSection(title = L("settings.section.map"))
        SwitchSettingsItem(
            title = "原生硬件加速地图 (120fps)",
            description = "使用 MapLibre 硬件加速渲染地图与所有实体，大幅提升交互与缩放流畅度",
            checked = nativeMapEnabled,
            onCheckedChange = { viewModel.setNativeMapEnabled(it) },
            isFirst = true
        )
        SwitchSettingsItem(
            title = L("settings.location.title"),
            description = L("settings.location.description"),
            checked = showLocation,
            onCheckedChange = { viewModel.setShowLocation(it) }
        )
        SwitchSettingsItem(
            title = L("settings.persistent_zoom.title"),
            description = L("settings.persistent_zoom.description"),
            checked = persistentZoom,
            onCheckedChange = { viewModel.setPersistentZoom(it) },
            isLast = true
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

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
