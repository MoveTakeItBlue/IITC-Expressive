package io.github.movetakeitblue.iitcexpressive.ui.settings

import androidx.compose.runtime.*
import io.github.movetakeitblue.iitcexpressive.i18n.L
import io.github.movetakeitblue.iitcexpressive.ui.common.ConfirmDialog
import io.github.movetakeitblue.iitcexpressive.ui.common.SelectFieldDialog
import io.github.movetakeitblue.iitcexpressive.viewmodel.SettingsViewModel

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
    val nativeMapEnabled by viewModel.nativeMapEnabled.collectAsState()
    val currentChannel by viewModel.currentChannel.collectAsState()
    val enabledPluginsCount by viewModel.enabledPluginsCount.collectAsState()
    val portalNameFontSize by viewModel.portalNameFontSize.collectAsState()
    val agentNameFontSize by viewModel.agentNameFontSize.collectAsState()
    val mapFontFamily by viewModel.mapFontFamily.collectAsState()
    val mapFontBold by viewModel.mapFontBold.collectAsState()
    val mapTextHalo by viewModel.mapTextHalo.collectAsState()
    val playerIconScale by viewModel.playerIconScale.collectAsState()

    var showPortalFontDialog by remember { mutableStateOf(false) }
    var showAgentFontDialog by remember { mutableStateOf(false) }
    var showFontFamilyDialog by remember { mutableStateOf(false) }
    var showPlayerIconScaleDialog by remember { mutableStateOf(false) }

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

    val fontSizes = listOf(10f, 11f, 12f, 13f, 14f, 15f, 16f)
    val fontSizeLabels = listOf("10 sp (偏小)", "11 sp", "12 sp (默认)", "13 sp", "14 sp (偏大)", "15 sp", "16 sp (特大)")
    val fontFamilies = listOf("sans-serif", "monospace", "serif")
    val fontFamilyLabels = listOf("系统默认 (Sans-Serif)", "等宽字体 (Monospace)", "衬线字体 (Serif)")

    if (showPortalFontDialog) {
        val selectedIdx = fontSizes.indexOf(portalNameFontSize).let { if (it >= 0) it else 2 }
        SelectFieldDialog(
            title = "选择 Portal 名称字号",
            items = fontSizeLabels,
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                viewModel.setPortalNameFontSize(fontSizes[idx])
            },
            onDismiss = { showPortalFontDialog = false }
        )
    }

    if (showAgentFontDialog) {
        val selectedIdx = fontSizes.indexOf(agentNameFontSize).let { if (it >= 0) it else 1 }
        SelectFieldDialog(
            title = "选择 Player 代号字号",
            items = fontSizeLabels,
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                viewModel.setAgentNameFontSize(fontSizes[idx])
            },
            onDismiss = { showAgentFontDialog = false }
        )
    }

    if (showFontFamilyDialog) {
        val selectedIdx = fontFamilies.indexOf(mapFontFamily).let { if (it >= 0) it else 0 }
        SelectFieldDialog(
            title = "选择地图文字字族",
            items = fontFamilyLabels,
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                viewModel.setMapFontFamily(fontFamilies[idx])
            },
            onDismiss = { showFontFamilyDialog = false }
        )
    }

    val playerIconScales = listOf(1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
    val playerIconScaleLabels = listOf("100% (标准)", "125%", "150% (默认 / 放大50%)", "175%", "200% (超大)")

    if (showPlayerIconScaleDialog) {
        val selectedIdx = playerIconScales.indexOf(playerIconScale).let { if (it >= 0) it else 2 }
        SelectFieldDialog(
            title = "选择 Player 图标大小",
            items = playerIconScaleLabels,
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                viewModel.setPlayerIconScale(playerIconScales[idx])
            },
            onDismiss = { showPlayerIconScaleDialog = false }
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

        // Map settings section
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

        // Map typography section
        SettingsSection(title = "地图文字与字体 (Map Typography)")
        NavSettingsItem(
            title = "Portal 名称字号",
            description = "${portalNameFontSize.toInt()} sp",
            isFirst = true,
            onClick = { showPortalFontDialog = true }
        )
        NavSettingsItem(
            title = "Player 代号字号",
            description = "${agentNameFontSize.toInt()} sp",
            onClick = { showAgentFontDialog = true }
        )
        NavSettingsItem(
            title = "Player 图标大小",
            description = when (playerIconScale) {
                1.0f -> "100% (标准)"
                1.25f -> "125%"
                1.5f -> "150% (默认)"
                1.75f -> "175%"
                2.0f -> "200% (超大)"
                else -> "${(playerIconScale * 100).toInt()}%"
            },
            onClick = { showPlayerIconScaleDialog = true }
        )
        NavSettingsItem(
            title = "地图文字字族",
            description = when (mapFontFamily) {
                "monospace" -> "等宽字体 (Monospace)"
                "serif" -> "衬线字体 (Serif)"
                else -> "系统默认 (Sans-Serif)"
            },
            onClick = { showFontFamilyDialog = true }
        )
        SwitchSettingsItem(
            title = "加粗地图文字",
            description = "使用加粗字重增强文字醒目度",
            checked = mapFontBold,
            onCheckedChange = { viewModel.setMapFontBold(it) }
        )
        SwitchSettingsItem(
            title = "文字微弱阴影描边",
            description = "默认关闭（纯净无底色）；在复杂卫星底图下开启可提供辅助微弱描边",
            checked = mapTextHalo,
            onCheckedChange = { viewModel.setMapTextHalo(it) },
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
