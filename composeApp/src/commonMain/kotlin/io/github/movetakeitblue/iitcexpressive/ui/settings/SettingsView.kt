package io.github.movetakeitblue.iitcexpressive.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val persistentZoom by viewModel.persistentZoom.collectAsState()
    val showLocation by viewModel.showLocation.collectAsState()
    val fullscreenMap by viewModel.fullscreenMap.collectAsState()
    val nativeMapEnabled by viewModel.nativeMapEnabled.collectAsState()
    val mapTextColor by viewModel.mapTextColor.collectAsState()
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
    var showMapTextColorDialog by remember { mutableStateOf(false) }
    var showCustomColorDialog by remember { mutableStateOf(false) }
    var customColorInput by remember { mutableStateOf("") }

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

    val colorKeys = listOf("auto", "white", "black", "amber", "cyan", "green", "custom")
    val colorLabels = listOf(
        "自动 (跟随底图反色 - 推荐)",
        "高光纯白 (#FFFFFF)",
        "深邃纯黑 (#000000)",
        "醒目琥珀黄 (#FFD600)",
        "科技青蓝 (#00E5FF)",
        "荧光翠绿 (#00FF66)",
        "自定义十六进制色值 (HEX)..."
    )

    if (showMapTextColorDialog) {
        val selectedIdx = when (mapTextColor.lowercase()) {
            "auto" -> 0
            "white" -> 1
            "black" -> 2
            "amber" -> 3
            "cyan" -> 4
            "green" -> 5
            else -> 6
        }
        SelectFieldDialog(
            title = "选择地图文字颜色",
            items = colorLabels,
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                if (idx == 6) {
                    customColorInput = if (mapTextColor !in listOf("auto", "white", "black", "amber", "cyan", "green")) mapTextColor else "#FFFFFF"
                    showCustomColorDialog = true
                } else {
                    viewModel.setMapTextColor(colorKeys[idx])
                }
                showMapTextColorDialog = false
            },
            onDismiss = { showMapTextColorDialog = false }
        )
    }

    if (showCustomColorDialog) {
        var tempInput by remember { mutableStateOf(customColorInput) }
        AlertDialog(
            onDismissRequest = { showCustomColorDialog = false },
            title = { Text("自定义地图文字颜色") },
            text = {
                Column {
                    Text(
                        text = "输入 6 位十六进制 HEX 颜色值（例如 #FFFFFF、#FFD600 或 #00FF66）：",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempInput,
                        onValueChange = { tempInput = it.trim() },
                        singleLine = true,
                        placeholder = { Text("#FFFFFF") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val formatted = if (tempInput.startsWith("#")) tempInput else "#$tempInput"
                        viewModel.setMapTextColor(formatted)
                        showCustomColorDialog = false
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomColorDialog = false }) {
                    Text("取消")
                }
            }
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
            title = "全面屏显示地图",
            description = "开启后地图延伸至顶部状态栏，提供更宽广的沉浸式视野",
            checked = fullscreenMap,
            onCheckedChange = { viewModel.setFullscreenMap(it) },
            isFirst = true
        )
        SwitchSettingsItem(
            title = "原生硬件加速地图 (120fps)",
            description = "使用 MapLibre 硬件加速渲染地图与所有实体，大幅提升交互与缩放流畅度",
            checked = nativeMapEnabled,
            onCheckedChange = { viewModel.setNativeMapEnabled(it) }
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
        val mapTextColorDesc = when (mapTextColor.lowercase()) {
            "auto" -> "自动 (跟随底图反色 - 推荐)"
            "white" -> "高光纯白 (#FFFFFF)"
            "black" -> "深邃纯黑 (#000000)"
            "amber" -> "醒目琥珀黄 (#FFD600)"
            "cyan" -> "科技青蓝 (#00E5FF)"
            "green" -> "荧光翠绿 (#00FF66)"
            else -> "自定义 (${mapTextColor.uppercase()})"
        }
        NavSettingsItem(
            title = "地图文字颜色",
            description = mapTextColorDesc,
            onClick = { showMapTextColorDialog = true }
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
            onCheckedChange = { viewModel.setZoomControl(it) },
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
