package io.github.movetakeitblue.iitcexpressive.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.movetakeitblue.iitcexpressive.i18n.L
import io.github.movetakeitblue.iitcexpressive.ui.common.SelectFieldDialog
import io.github.movetakeitblue.iitcexpressive.viewmodel.MainViewModel
import io.github.movetakeitblue.iitcexpressive.viewmodel.SettingsViewModel

data class ColorPresetItem(
    val id: String,
    val nameKey: String,
    val swatchColor: Color
)

@Composable
fun AppearanceView(
    viewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDynamicColor by viewModel.isDynamicColor.collectAsState()
    val presetColor by viewModel.presetColor.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    val fullscreenMap by settingsViewModel.fullscreenMap.collectAsState()
    val zoomControl by settingsViewModel.zoomControl.collectAsState()
    val mapTextColor by settingsViewModel.mapTextColor.collectAsState()
    val portalNameFontSize by settingsViewModel.portalNameFontSize.collectAsState()
    val agentNameFontSize by settingsViewModel.agentNameFontSize.collectAsState()
    val mapFontFamily by settingsViewModel.mapFontFamily.collectAsState()
    val mapFontBold by settingsViewModel.mapFontBold.collectAsState()
    val mapTextHalo by settingsViewModel.mapTextHalo.collectAsState()
    val playerIconScale by settingsViewModel.playerIconScale.collectAsState()

    var showThemeModeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPresetColorDialog by remember { mutableStateOf(false) }
    var showPortalFontDialog by remember { mutableStateOf(false) }
    var showAgentFontDialog by remember { mutableStateOf(false) }
    var showFontFamilyDialog by remember { mutableStateOf(false) }
    var showPlayerIconScaleDialog by remember { mutableStateOf(false) }
    var showMapTextColorDialog by remember { mutableStateOf(false) }
    var showCustomColorDialog by remember { mutableStateOf(false) }
    var customColorInput by remember { mutableStateOf("") }

    val themeModes = listOf("system", "light", "dark")
    val themeModeLabels = listOf(
        L("appearance.theme.system"),
        L("appearance.theme.light"),
        L("appearance.theme.dark")
    )

    if (showThemeModeDialog) {
        val selectedIdx = themeModes.indexOf(themeMode).let { if (it >= 0) it else 0 }
        SelectFieldDialog(
            title = L("appearance.section.theme_mode"),
            items = themeModeLabels,
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                viewModel.setThemeMode(themeModes[idx])
                showThemeModeDialog = false
            },
            onDismiss = { showThemeModeDialog = false }
        )
    }

    val languageKeys = listOf("system", "zh-CN", "zh-HK", "zh-TW", "en", "ja")
    val languageLabels = listOf(
        L("appearance.language.system"),
        L("appearance.language.zh_cn"),
        L("appearance.language.zh_hk"),
        L("appearance.language.zh_tw"),
        L("appearance.language.en"),
        L("appearance.language.ja")
    )

    if (showLanguageDialog) {
        val selectedIdx = languageKeys.indexOf(appLanguage).let { if (it >= 0) it else 0 }
        SelectFieldDialog(
            title = L("appearance.language.title"),
            items = languageLabels,
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                viewModel.setAppLanguage(languageKeys[idx])
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    val presetItems = listOf(
        ColorPresetItem("blue", "appearance.color.blue", Color(0xFF0088FF)),
        ColorPresetItem("green", "appearance.color.green", Color(0xFF00CC44)),
        ColorPresetItem("red", "appearance.color.red", Color(0xFFFF3344)),
        ColorPresetItem("purple", "appearance.color.purple", Color(0xFF9E77ED)),
        ColorPresetItem("amber", "appearance.color.amber", Color(0xFFFFB300)),
        ColorPresetItem("teal", "appearance.color.teal", Color(0xFF006A5F)),
        ColorPresetItem("slate", "appearance.color.slate", Color(0xFF607D8B))
    )

    if (showPresetColorDialog) {
        val selectedIdx = presetItems.indexOfFirst { it.id.equals(presetColor, ignoreCase = true) }.let { if (it >= 0) it else 5 }
        SelectFieldDialog(
            title = L("appearance.preset_colors.title"),
            items = presetItems.map { L(it.nameKey) },
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                viewModel.setPresetColor(presetItems[idx].id)
                viewModel.setDynamicColor(false)
                showPresetColorDialog = false
            },
            onDismiss = { showPresetColorDialog = false }
        )
    }

    val fontSizes = listOf(10f, 11f, 12f, 13f, 14f, 15f, 16f)
    val fontSizeLabels = listOf("10 sp (偏小)", "11 sp", "12 sp (默认)", "13 sp", "14 sp (偏大)", "15 sp", "16 sp (特大)")
    val fontFamilies = listOf("sans-serif", "monospace", "serif")
    val fontFamilyLabels = listOf("系统默认 (Sans-Serif)", "等宽字体 (Monospace)", "衬线字体 (Serif)")

    if (showPortalFontDialog) {
        val selectedIdx = fontSizes.indexOf(portalNameFontSize).let { if (it >= 0) it else 2 }
        SelectFieldDialog(
            title = L("appearance.portal_font_size.title"),
            items = fontSizeLabels,
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                settingsViewModel.setPortalNameFontSize(fontSizes[idx])
                showPortalFontDialog = false
            },
            onDismiss = { showPortalFontDialog = false }
        )
    }

    if (showAgentFontDialog) {
        val selectedIdx = fontSizes.indexOf(agentNameFontSize).let { if (it >= 0) it else 1 }
        SelectFieldDialog(
            title = L("appearance.agent_font_size.title"),
            items = fontSizeLabels,
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                settingsViewModel.setAgentNameFontSize(fontSizes[idx])
                showAgentFontDialog = false
            },
            onDismiss = { showAgentFontDialog = false }
        )
    }

    if (showFontFamilyDialog) {
        val selectedIdx = fontFamilies.indexOf(mapFontFamily).let { if (it >= 0) it else 0 }
        SelectFieldDialog(
            title = L("appearance.font_family.title"),
            items = fontFamilyLabels,
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                settingsViewModel.setMapFontFamily(fontFamilies[idx])
                showFontFamilyDialog = false
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
            title = L("appearance.map_text_color.title"),
            items = colorLabels,
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                if (idx == 6) {
                    customColorInput = if (mapTextColor !in listOf("auto", "white", "black", "amber", "cyan", "green")) mapTextColor else "#FFFFFF"
                    showCustomColorDialog = true
                } else {
                    settingsViewModel.setMapTextColor(colorKeys[idx])
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
            title = { Text(L("appearance.map_text_color.title")) },
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
                        settingsViewModel.setMapTextColor(formatted)
                        showCustomColorDialog = false
                    }
                ) {
                    Text(L("dialog.ok"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomColorDialog = false }) {
                    Text(L("dialog.cancel"))
                }
            }
        )
    }

    val playerIconScales = listOf(1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
    val playerIconScaleLabels = listOf("100% (标准)", "125%", "150% (默认 / 放大50%)", "175%", "200% (超大)")

    if (showPlayerIconScaleDialog) {
        val selectedIdx = playerIconScales.indexOf(playerIconScale).let { if (it >= 0) it else 2 }
        SelectFieldDialog(
            title = L("appearance.player_icon_scale.title"),
            items = playerIconScaleLabels,
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                settingsViewModel.setPlayerIconScale(playerIconScales[idx])
                showPlayerIconScaleDialog = false
            },
            onDismiss = { showPlayerIconScaleDialog = false }
        )
    }

    SettingsBase(
        title = L("appearance.title"),
        onBack = onBack
    ) {
        // Theme Mode Section
        SettingsSection(title = L("appearance.section.theme_mode"))
        val currentThemeLabel = when (themeMode) {
            "light" -> L("appearance.theme.light")
            "dark" -> L("appearance.theme.dark")
            else -> L("appearance.theme.system")
        }
        NavSettingsItem(
            title = L("appearance.section.theme_mode"),
            description = currentThemeLabel,
            isFirst = true,
            isLast = false,
            onClick = { showThemeModeDialog = true }
        )

        // Theme Mode Segmented Choice Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themeModes.forEachIndexed { index, mode ->
                    val isSelected = themeMode == mode
                    val itemBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(itemBg)
                            .clickable { viewModel.setThemeMode(mode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = themeModeLabels[index],
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Colors Section
        SettingsSection(title = L("appearance.section.colors"))
        SwitchSettingsItem(
            title = L("appearance.dynamic_color.title"),
            description = L("appearance.dynamic_color.desc"),
            checked = isDynamicColor,
            onCheckedChange = { viewModel.setDynamicColor(it) },
            isFirst = true,
            isLast = false
        )

        val activePresetItem = presetItems.find { it.id.equals(presetColor, ignoreCase = true) } ?: presetItems[5]
        NavSettingsItem(
            title = L("appearance.preset_colors.title"),
            description = if (isDynamicColor) "已开启动态提取 (点击可切换为固定预设强调色)" else L(activePresetItem.nameKey),
            isFirst = false,
            isLast = false,
            onClick = { showPresetColorDialog = true }
        )

        // Color Swatches Row Container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presetItems.forEach { preset ->
                    val isSelected = !isDynamicColor && presetColor.equals(preset.id, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(preset.swatchColor)
                            .then(
                                if (isSelected) {
                                    Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                } else {
                                    Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), CircleShape)
                                }
                            )
                            .clickable {
                                viewModel.setPresetColor(preset.id)
                                viewModel.setDynamicColor(false)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language Section
        SettingsSection(title = L("appearance.section.language"))
        val currentLangIndex = languageKeys.indexOf(appLanguage).let { if (it >= 0) it else 0 }
        NavSettingsItem(
            title = L("appearance.language.title"),
            description = languageLabels[currentLangIndex],
            isFirst = true,
            isLast = true,
            onClick = { showLanguageDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Map Display & Typography Section
        SettingsSection(title = L("appearance.section.map_display"))
        SwitchSettingsItem(
            title = L("appearance.fullscreen_map.title"),
            description = L("appearance.fullscreen_map.description"),
            checked = fullscreenMap,
            onCheckedChange = { settingsViewModel.setFullscreenMap(it) },
            isFirst = true
        )
        SwitchSettingsItem(
            title = L("appearance.zoom_controls.title"),
            description = L("appearance.zoom_controls.description"),
            checked = zoomControl,
            onCheckedChange = { settingsViewModel.setZoomControl(it) }
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
            title = L("appearance.map_text_color.title"),
            description = mapTextColorDesc,
            onClick = { showMapTextColorDialog = true }
        )
        NavSettingsItem(
            title = L("appearance.portal_font_size.title"),
            description = "${portalNameFontSize.toInt()} sp",
            onClick = { showPortalFontDialog = true }
        )
        NavSettingsItem(
            title = L("appearance.agent_font_size.title"),
            description = "${agentNameFontSize.toInt()} sp",
            onClick = { showAgentFontDialog = true }
        )
        NavSettingsItem(
            title = L("appearance.player_icon_scale.title"),
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
            title = L("appearance.font_family.title"),
            description = when (mapFontFamily) {
                "monospace" -> "等宽字体 (Monospace)"
                "serif" -> "衬线字体 (Serif)"
                else -> "系统默认 (Sans-Serif)"
            },
            onClick = { showFontFamilyDialog = true }
        )
        SwitchSettingsItem(
            title = L("appearance.font_bold.title"),
            description = "使用加粗字重增强文字醒目度",
            checked = mapFontBold,
            onCheckedChange = { settingsViewModel.setMapFontBold(it) }
        )
        SwitchSettingsItem(
            title = L("appearance.text_halo.title"),
            description = "在复杂底图下开启可提供辅助微弱描边",
            checked = mapTextHalo,
            onCheckedChange = { settingsViewModel.setMapTextHalo(it) },
            isLast = true
        )
    }
}
