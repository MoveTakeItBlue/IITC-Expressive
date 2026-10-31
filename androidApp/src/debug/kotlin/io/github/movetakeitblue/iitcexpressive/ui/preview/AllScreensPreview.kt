package io.github.movetakeitblue.iitcexpressive.ui.preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.movetakeitblue.iitcexpressive.manager.BackupManager
import io.github.movetakeitblue.iitcexpressive.manager.IITCManager
import io.github.movetakeitblue.iitcexpressive.model.*
import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils
import io.github.movetakeitblue.iitcexpressive.storage.AppStorage
import io.github.movetakeitblue.iitcexpressive.theme.*
import io.github.movetakeitblue.iitcexpressive.ui.chat.ChatSheet
import io.github.movetakeitblue.iitcexpressive.ui.common.ConfirmDialog
import io.github.movetakeitblue.iitcexpressive.ui.common.LocationShareDialog
import io.github.movetakeitblue.iitcexpressive.ui.debug.DebugConsole
import io.github.movetakeitblue.iitcexpressive.ui.layers.LayersSheet
import io.github.movetakeitblue.iitcexpressive.ui.menu.FabMenu
import io.github.movetakeitblue.iitcexpressive.ui.portal.*
import io.github.movetakeitblue.iitcexpressive.ui.settings.*
import io.github.movetakeitblue.iitcexpressive.viewmodel.*
import kotlinx.serialization.json.*

/**
 * =========================================================================================
 *  IITC-Expressive 全页面 / 全组件 Compose Preview 统一合集
 * =========================================================================================
 *  功能：
 *    在离线无网络、无需启动模拟器或调试器的环境下，通过 Android Studio Split / Design 面板
 *    直接查看各页面的 Material 3 Expressive 设计规范、卡片样式、面板展开与对话框状态。
 *
 *  目录结构与涵盖页面：
 *    1. 地图主框架与 FAB (Main Map & FAB Menu)
 *    2. Portal 详情与 Mods 模块 (Portal Details & Mods Sheet / Dialog)
 *    3. 图层与高亮配置抽屉 (Layers & Highlighters Sheet)
 *    4. COMM 聊天频道 (Chat Sheet - All / Faction)
 *    5. 设置与插件全套页面 (Settings, Plugins, Info, Add, UpdateChannel, Backup, About, Licenses)
 *    6. 调试控制台与通用弹窗 (Debug Console, LocationShare, Confirm)
 * =========================================================================================
 */

// -----------------------------------------------------------------------------------------
//  1. 预览基础底座与 Mock 状态构建器
// -----------------------------------------------------------------------------------------

@Composable
private fun PreviewTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IITCDarkColorScheme,
        shapes = IITCExpressiveShapes
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            content()
        }
    }
}

/** 模拟地图底图，展示暗黑网格与虚构的 Ingress Links / Portals */
@Composable
private fun FakeMap(modifier: Modifier = Modifier) {
    val grid = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    val resColor = Color(0xFF70D2FF)
    val enlColor = Color(0xFF66DC96)
    val macColor = Color(0xFFFF7164)

    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF0F1416), Color(0xFF0A0D0E))
            )
        )
        val step = 56.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(grid, Offset(x, 0f), Offset(x, size.height), 1f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(grid, Offset(0f, y), Offset(size.width, y), 1f)
            y += step
        }

        // Links
        val p1 = Offset(size.width * 0.22f, size.height * 0.28f)
        val p2 = Offset(size.width * 0.78f, size.height * 0.22f)
        val p3 = Offset(size.width * 0.42f, size.height * 0.65f)
        val p4 = Offset(size.width * 0.82f, size.height * 0.75f)

        drawLine(resColor.copy(alpha = 0.6f), p1, p2, 2.5.dp.toPx())
        drawLine(resColor.copy(alpha = 0.6f), p1, p3, 2.5.dp.toPx())
        drawLine(resColor.copy(alpha = 0.6f), p2, p3, 2.5.dp.toPx())
        drawLine(enlColor.copy(alpha = 0.6f), p3, p4, 2.5.dp.toPx())

        // Portals
        listOf(
            p1 to resColor,
            p2 to resColor,
            p3 to resColor,
            p4 to enlColor,
            Offset(size.width * 0.15f, size.height * 0.78f) to macColor
        ).forEach { (center, color) ->
            drawCircle(color.copy(alpha = 0.18f), radius = 32.dp.toPx(), center = center)
            drawCircle(color, radius = 6.dp.toPx(), center = center)
        }
    }
}

private val previewModsSample = listOf(
    PortalMod(
        name = "Aegis Shield",
        rarity = "VERY RARE",
        owner = "AzureKnight",
        stats = mapOf("Mitigation" to "+70", "Deployed" to "2026-10-25")
    ),
    PortalMod(
        name = "Multi-hack",
        rarity = "RARE",
        owner = "NeoMatrix",
        stats = mapOf("Hack count" to "+4", "Deployed" to "2026-10-24")
    ),
    PortalMod(
        name = "Heat Sink",
        rarity = "RARE",
        owner = "SilentPath",
        stats = mapOf("Cooldown" to "-50%", "Deployed" to "2026-10-24")
    ),
    PortalMod(
        name = "Force Amp",
        rarity = "COMMON",
        owner = "GhostRunner",
        stats = mapOf("Attack Force" to "x2", "Deployed" to "2026-10-23")
    )
)

/** 构造带全套真实 Mock 数据的 MainViewModel */
private fun createMockMainViewModel(
    hasSelectedPortal: Boolean = true,
    isPortalExpanded: Boolean = false,
    isFabExpanded: Boolean = false
): MainViewModel {
    val storage = AppStorage.createInMemory()
    val manager = IITCManager(storage)
    val vm = MainViewModel(storage, manager)

    if (hasSelectedPortal) {
        vm.handleBridgeMessage(
            "setPortalDetails",
            buildJsonObject {
                put("guid", "portal_oriental_pearl")
                putJsonObject("details") {
                    put("title", "Oriental Pearl Radio & TV Tower")
                    put("team", "RES")
                    put("level", 8)
                    put("health", 95)
                    put("owner", "AzureKnight")
                    put("address", "1 Century Ave, Lujiazui, Pudong, Shanghai")
                    put("rangeMeters", 1250000L)
                    put("linksIn", 12)
                    put("linksOut", 8)
                    put("fieldsCount", 15)
                    put("energy", 48000L)
                    put("maxEnergy", 48000L)
                    put("apGain", 1850L)
                    put("historyVisited", true)
                    put("historyCaptured", true)
                    put("historyScoutControlled", false)
                    putJsonArray("resonators") {
                        repeat(8) {
                            addJsonObject {
                                put("level", 8)
                                put("energy", 6000)
                                put("owner", "AzureKnight")
                            }
                        }
                    }
                    putJsonArray("mods") {
                        previewModsSample.forEach { mod ->
                            addJsonObject {
                                put("name", mod.name)
                                put("rarity", mod.rarity)
                                put("owner", mod.owner)
                            }
                        }
                    }
                    putJsonArray("linkedPortals") {
                        addJsonObject {
                            put("guid", "portal_shanghai_tower")
                            put("title", "Shanghai Tower (World #2)")
                            put("lat", 31.2335)
                            put("lng", 121.5056)
                            put("isOutgoing", true)
                            put("team", "RES")
                            put("level", 8)
                            put("health", 100)
                            put("distance", 1420L)
                        }
                        addJsonObject {
                            put("guid", "portal_jin_mao")
                            put("title", "Jin Mao Tower Observation Deck")
                            put("lat", 31.2372)
                            put("lng", 121.5012)
                            put("isOutgoing", true)
                            put("team", "RES")
                            put("level", 7)
                            put("health", 85)
                            put("distance", 980L)
                        }
                        addJsonObject {
                            put("guid", "portal_bund_bull")
                            put("title", "Bund Financial Bull")
                            put("lat", 31.2312)
                            put("lng", 121.4921)
                            put("isOutgoing", false)
                            put("team", "ENL")
                            put("level", 8)
                            put("health", 90)
                            put("distance", 2150L)
                        }
                    }
                }
            }
        )
    }

    if (isPortalExpanded) {
        vm.expandPortalDetails()
    }

    if (isFabExpanded) {
        vm.toggleFabMenu()
    }

    vm.setPlayerTrackerEnabled(true)
    vm.handleBridgeMessage(
        "setChatMessages",
        buildJsonObject {
            put("channel", "all")
            putJsonArray("messages") {
                addJsonObject {
                    put("id", "msg_1")
                    put("time", PlatformUtils.currentTimeMillis() - 15 * 60 * 1000)
                    put("sender", "AzureKnight")
                    put("team", "RESISTANCE")
                    put("text", "Captured Oriental Pearl Tower")
                    putJsonObject("portal") {
                        put("name", "Oriental Pearl Tower")
                        put("lat", 31.2397)
                        put("lng", 121.4998)
                    }
                }
                addJsonObject {
                    put("id", "msg_2")
                    put("time", PlatformUtils.currentTimeMillis() - 5 * 60 * 1000)
                    put("sender", "AzureKnight")
                    put("team", "RESISTANCE")
                    put("text", "Linked to Shanghai Tower")
                    putJsonObject("portal") {
                        put("name", "Shanghai Tower")
                        put("lat", 31.2335)
                        put("lng", 121.5056)
                    }
                }
                addJsonObject {
                    put("id", "msg_3")
                    put("time", PlatformUtils.currentTimeMillis() - 8 * 60 * 1000)
                    put("sender", "EmeraldFox")
                    put("team", "ENLIGHTENED")
                    put("text", "Destroyed resonator")
                    putJsonObject("portal") {
                        put("name", "Bund Financial Bull")
                        put("lat", 31.2312)
                        put("lng", 121.4921)
                    }
                }
            }
        }
    )

    // Mock Layers
    vm.handleBridgeMessage(
        "setLayers",
        buildJsonObject {
            put("base_layer", """[
                {"layerId": 0, "name": "CartoDB Dark Matter", "active": true},
                {"layerId": 1, "name": "CartoDB Positron", "active": false},
                {"layerId": 2, "name": "OpenStreetMap", "active": false},
                {"layerId": 3, "name": "Google Default", "active": false}
            ]""")
            put("overlay_layer", """[
                {"layerId": 10, "name": "Portals", "active": true},
                {"layerId": 11, "name": "Links", "active": true},
                {"layerId": 12, "name": "Fields", "active": true},
                {"layerId": 13, "name": "Portal Highlighters", "active": true},
                {"layerId": 14, "name": "Draw Tools", "active": true},
                {"layerId": 15, "name": "Unclaimed Portals", "active": false},
                {"layerId": 16, "name": "Artifacts & Ornaments", "active": true}
            ]""")
        }
    )

    // Mock Highlighters
    vm.handleBridgeMessage("addPortalHighlighter", buildJsonObject { put("name", "Portal Level") })
    vm.handleBridgeMessage("addPortalHighlighter", buildJsonObject { put("name", "High Level Portals") })
    vm.handleBridgeMessage("addPortalHighlighter", buildJsonObject { put("name", "Needs Recharge") })
    vm.handleBridgeMessage("setActiveHighlighter", buildJsonObject { put("name", "High Level Portals") })

    // Mock Chat Messages
    vm.handleBridgeMessage(
        "setChatMessages",
        buildJsonObject {
            put("channel", "all")
            putJsonArray("messages") {
                addJsonObject {
                    put("id", "msg_1")
                    put("time", 1729934400000L)
                    put("formattedTime", "14:20")
                    put("sender", "GreenDragon")
                    put("team", "ENLIGHTENED")
                    put("text", "Captured Century Park Gate")
                    put("isSecure", false)
                    putJsonObject("portal") {
                        put("name", "Century Park Gate")
                        put("lat", 31.22)
                        put("lng", 121.55)
                    }
                }
                addJsonObject {
                    put("id", "msg_2")
                    put("time", 1729934460000L)
                    put("formattedTime", "14:21")
                    put("sender", "AzureKnight")
                    put("team", "RESISTANCE")
                    put("text", "Deploying Aegis Shield at Oriental Pearl!")
                    put("isSecure", false)
                }
                addJsonObject {
                    put("id", "msg_3")
                    put("time", 1729934520000L)
                    put("formattedTime", "14:23")
                    put("sender", "[Machina]")
                    put("team", "MAC")
                    put("text", "Infected Central Plaza")
                    put("isSecure", false)
                }
            }
        }
    )

    // Mock Logs
    vm.addLog("info", "IITC Expressive v1.2.0 initialized", "System")
    vm.addLog("info", "MapLibre Native 120fps engine active", "NativeMap")
    vm.addLog("debug", "Synchronized 142 portals, 38 links, 14 fields", "IntelSync")
    vm.addLog("warn", "Tile cache fresh: next refresh in 30s", "Network")

    return vm
}

/** 构造带示例插件的 PluginsViewModel */
private fun createMockPluginsViewModel(): PluginsViewModel {
    val storage = AppStorage.createInMemory()
    val manager = IITCManager(storage)
    manager.setMockPlugins(
        listOf(
            Plugin(
                uid = "draw-tools",
                name = "Draw tools",
                category = "Map",
                version = "0.7.0",
                description = "Draw lines, polygons, and circles directly on the map for field planning",
                author = "breunigs",
                status = "on"
            ),
            Plugin(
                uid = "bookmarks",
                name = "Bookmarks for maps and portals",
                category = "Controls",
                version = "0.3.2",
                description = "Save portals and map views for instant one-click access",
                author = "Zaso",
                status = "on"
            ),
            Plugin(
                uid = "cross-links",
                name = "Cross Links",
                category = "Layer",
                version = "1.1.2",
                description = "Highlight links that cross currently drawn plans",
                author = "IITC Community",
                status = "off"
            ),
            Plugin(
                uid = "guess-player-level",
                name = "Guess Player Level",
                category = "Info",
                version = "0.6.1",
                description = "Estimate player level based on deployed resonators in COMM",
                author = "breunigs",
                status = "on"
            ),
            Plugin(
                uid = "portal-names",
                name = "Portal Names",
                category = "Layer",
                version = "0.1.4",
                description = "Show portal names on map when zoomed in",
                author = "jonatkins",
                status = "off"
            )
        )
    )
    return PluginsViewModel(manager)
}

// -----------------------------------------------------------------------------------------
//  2. 地图主框架与 FAB 菜单预览 (Main Map & FAB Menu)
// -----------------------------------------------------------------------------------------

@Preview(name = "01 · 地图主界面 (默认未选中 Portal)", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
fun Preview_01_MainMap_Default() {
    val vm = remember { createMockMainViewModel(hasSelectedPortal = false) }
    PreviewTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            FakeMap(Modifier.fillMaxSize())
            FabMenu(
                isExpanded = false,
                onToggle = {},
                onNavigateToSettings = {},
                onNavigateToPlugins = {},
                onOpenLayers = {},
                onOpenChat = {},
                onLocate = {},
                onDebug = {},
                onReload = {},
                bottomOffset = 16.dp,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(name = "02 · 地图主界面 (选中 Portal，FAB 上移避让)", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
fun Preview_02_MainMap_PortalSelected() {
    val vm = remember { createMockMainViewModel(hasSelectedPortal = true, isPortalExpanded = false) }
    PreviewTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            FakeMap(Modifier.fillMaxSize())
            PortalDetailsSheet(
                viewModel = vm,
                modifier = Modifier.fillMaxSize()
            )
            FabMenu(
                isExpanded = false,
                onToggle = {},
                onNavigateToSettings = {},
                onNavigateToPlugins = {},
                onOpenLayers = {},
                onOpenChat = {},
                onLocate = {},
                onDebug = {},
                onReload = {},
                bottomOffset = PortalSheetDefaults.PeekHeight + PortalSheetDefaults.FabGap,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(name = "03 · FAB 菜单展开状态 (全部快捷操作)", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
fun Preview_03_FabMenu_Expanded() {
    val vm = remember { createMockMainViewModel(hasSelectedPortal = false, isFabExpanded = true) }
    PreviewTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            FakeMap(Modifier.fillMaxSize())
            FabMenu(
                isExpanded = true,
                onToggle = {},
                onNavigateToSettings = {},
                onNavigateToPlugins = {},
                onOpenLayers = {},
                onOpenChat = {},
                onLocate = {},
                onDebug = {},
                onReload = {},
                bottomOffset = 16.dp,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
//  3. Portal 详情与 Mods 模块预览 (Portal Details & Mods)
// -----------------------------------------------------------------------------------------

@Preview(name = "04 · Portal 底部摘要条 (PeekBar)", widthDp = 411, showBackground = true)
@Composable
fun Preview_04_PortalSheet_Peek() {
    val vm = remember { createMockMainViewModel(hasSelectedPortal = true, isPortalExpanded = false) }
    PreviewTheme {
        Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.BottomCenter) {
            PortalDetailsSheet(
                viewModel = vm,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(name = "05 · Portal 详情完整展开面板 (Expanded Sheet)", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
fun Preview_05_PortalSheet_Expanded() {
    val vm = remember { createMockMainViewModel(hasSelectedPortal = true, isPortalExpanded = true) }
    PreviewTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            FakeMap(Modifier.fillMaxSize())
            PortalDetailsSheet(
                viewModel = vm,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(name = "06 · Portal Mod 详情弹窗 (Mod Detail Dialog)", widthDp = 411, heightDp = 640, showBackground = true)
@Composable
fun Preview_06_PortalModDetail_Dialog() {
    PreviewTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            PortalModDetailDialog(
                mod = previewModsSample.first(),
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "07 · Portal 4槽位 Mods 卡片行 (Mods Row)", widthDp = 411, showBackground = true)
@Composable
fun Preview_07_PortalMods_Row() {
    PreviewTheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PortalModsHeader()
            PortalModsRow(
                mods = previewModsSample,
                onModClick = {}
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
//  4. 图层与高亮选择抽屉 (Layers & Highlighters Sheet)
// -----------------------------------------------------------------------------------------

@Preview(name = "08 · 图层与高亮配置面板 (Layers Sheet)", widthDp = 411, heightDp = 800, showBackground = true)
@Composable
fun Preview_08_LayersSheet() {
    val vm = remember { createMockMainViewModel() }
    PreviewTheme {
        LayersSheet(
            viewModel = vm,
            onDismiss = {}
        )
    }
}

// -----------------------------------------------------------------------------------------
//  5. COMM 聊天频道 (Chat Sheet - All & Faction)
// -----------------------------------------------------------------------------------------

@Preview(name = "09 · COMM 聊天面板 (全部频道 All)", widthDp = 411, heightDp = 800, showBackground = true)
@Composable
fun Preview_09_ChatSheet_All() {
    val vm = remember { createMockMainViewModel() }
    PreviewTheme {
        ChatSheet(
            viewModel = vm,
            onDismiss = {}
        )
    }
}

// -----------------------------------------------------------------------------------------
//  6. 设置与插件全套页面预览 (Settings & Plugins Suite)
// -----------------------------------------------------------------------------------------

@Preview(name = "10 · 主设置页面 (Settings View 含 120fps 原生开关)", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
fun Preview_10_SettingsView() {
    val mainVm = remember { createMockMainViewModel() }
    val settingsVm = remember { SettingsViewModel(mainVm.storage, mainVm) }
    PreviewTheme {
        SettingsView(
            viewModel = settingsVm,
            onBack = {},
            onNavigateToAppearance = {},
            onNavigateToPlugins = {},
            onNavigateToUpdateChannel = {},
            onNavigateToBackup = {},
            onNavigateToAbout = {},
            onNavigateToLicenses = {}
        )
    }
}

@Preview(name = "11 · 插件管理中心 (Plugins View)", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
fun Preview_11_PluginsView() {
    val pluginsVm = remember { createMockPluginsViewModel() }
    PreviewTheme {
        PluginsView(
            viewModel = pluginsVm,
            onBack = {}
        )
    }
}

@Preview(name = "12 · 插件详情弹窗 (Plugin Info Sheet)", widthDp = 411, heightDp = 640, showBackground = true)
@Composable
fun Preview_12_PluginInfoSheet() {
    val samplePlugin = Plugin(
        uid = "draw-tools",
        name = "Draw tools",
        category = "Map",
        version = "0.7.0",
        description = "Draw lines, polygons, and circles on the map. Supports layer export and cross-links detection.",
        author = "breunigs",
        status = "on"
    )
    PreviewTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            PluginInfoSheet(
                plugin = samplePlugin,
                installMode = false,
                onInstall = {},
                onDelete = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "13 · 添加第三方插件弹窗 (Add Plugin Sheet)", widthDp = 411, heightDp = 400, showBackground = true)
@Composable
fun Preview_13_AddPluginSheet() {
    PreviewTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            AddPluginSheet(
                initialUrl = "https://example.com/iitc-plugin-custom.user.js",
                onLoadUrl = {},
                onChooseFile = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "14 · 更新通道设置页 (Update Channel View)", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
fun Preview_14_UpdateChannelView() {
    val storage = remember { AppStorage.createInMemory() }
    val manager = remember { IITCManager(storage) }
    val updateVm = remember { UpdateChannelViewModel(manager, storage) }
    PreviewTheme {
        UpdateChannelView(
            viewModel = updateVm,
            onBack = {}
        )
    }
}

@Preview(name = "15 · 备份与恢复页面 (Backup & Restore View)", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
fun Preview_15_BackupView() {
    val storage = remember { AppStorage.createInMemory() }
    val manager = remember { IITCManager(storage) }
    val backupMgr = remember { BackupManager(storage, manager) }
    val backupVm = remember { BackupViewModel(backupMgr) }
    PreviewTheme {
        BackupView(
            viewModel = backupVm,
            onBack = {}
        )
    }
}

@Preview(name = "16 · M3E 关于页面 (About View)", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
fun Preview_16_AboutView() {
    PreviewTheme {
        AboutView(onBack = {})
    }
}

@Preview(name = "17 · 开源许可协议页面 (Licenses View)", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
fun Preview_17_LicensesView() {
    PreviewTheme {
        LicensesView(onBack = {})
    }
}

// -----------------------------------------------------------------------------------------
//  7. 调试控制台与通用对话框预览 (Debug & Common Dialogs)
// -----------------------------------------------------------------------------------------

@Preview(name = "18 · 开发者调试控制台 (Debug Console)", widthDp = 411, heightDp = 640, showBackground = true)
@Composable
fun Preview_18_DebugConsole() {
    val vm = remember { createMockMainViewModel() }
    PreviewTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            DebugConsole(
                viewModel = vm,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(name = "19 · 坐标与 Portal 分享弹窗 (Location Share Dialog)", widthDp = 411, heightDp = 500, showBackground = true)
@Composable
fun Preview_19_LocationShareDialog() {
    PreviewTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LocationShareDialog(
                dialog = AppDialog.LocationShare(
                    lat = 31.2397,
                    lng = 121.4998,
                    title = "Oriental Pearl Radio & TV Tower",
                    isPortal = true,
                    guid = "guid_shanghai_pearl"
                ),
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "20 · 确认操作弹窗 (Confirm Dialog)", widthDp = 411, heightDp = 400, showBackground = true)
@Composable
fun Preview_20_ConfirmDialog() {
    PreviewTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ConfirmDialog(
                title = "清除会话凭据",
                message = "确定要清除所有已保存的 Cookies 与会话信息吗？清除后将需要重新登录 Ingress Intel。",
                onConfirm = {},
                onCancel = {}
            )
        }
    }
}
