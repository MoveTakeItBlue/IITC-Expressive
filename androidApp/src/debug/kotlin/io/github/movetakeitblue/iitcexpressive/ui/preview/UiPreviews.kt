package io.github.movetakeitblue.iitcexpressive.ui.preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import io.github.movetakeitblue.iitcexpressive.model.PortalMod
import io.github.movetakeitblue.iitcexpressive.theme.IITCDarkColorScheme
import io.github.movetakeitblue.iitcexpressive.theme.IITCExpressiveShapes
import io.github.movetakeitblue.iitcexpressive.ui.menu.FabMenu
import io.github.movetakeitblue.iitcexpressive.ui.portal.PortalModDetailDialog
import io.github.movetakeitblue.iitcexpressive.ui.portal.PortalModsHeader
import io.github.movetakeitblue.iitcexpressive.ui.portal.PortalModsRow
import io.github.movetakeitblue.iitcexpressive.ui.portal.PortalSheetDefaults

/**
 * ============================================================
 *  Compose Preview 合集
 * ============================================================
 *  用途：在 Android Studio 的 Split / Design 面板里直接查看本次 4 项 UI 改动，
 *        不需要跑模拟器，也不需要开调试。
 *
 *  打开方式：Android Studio 右侧「Gradle」/ 顶部工具栏的 Split 视图，
 *            选中文件后点 Design 标签即可看到所有 @Preview。
 *
 *  说明：预览环境里没有真实的系统栏 inset（WindowInsets 恒为 0），
 *        所以地图相关的预览用固定的假 inset（24dp / 24dp）来还原真机几何，
 *        生产代码仍由 windowInsetsPadding 读取真实 inset。
 * ============================================================
 */

private const val PREVIEW_STATUSBAR_DP = 24
private const val PREVIEW_NAVBAR_DP = 24

// ---------------------------------------------------------------- 预览数据

private val previewMods = listOf(
    PortalMod(
        name = "Portal Shield",
        rarity = "VERY RARE",
        owner = "KaiTheAgent",
        stats = mapOf("Mitigation" to "+60", "Deployed" to "2026-10-24")
    ),
    PortalMod(
        name = "Multi-hack",
        rarity = "RARE",
        owner = "NeonRunner",
        stats = mapOf("Hack count" to "+4", "Deployed" to "2026-10-23")
    ),
    PortalMod(
        name = "Heat Sink",
        rarity = "COMMON",
        owner = "SilentPath",
        stats = mapOf("Cooldown" to "-50%", "Deployed" to "2026-10-22")
    )
)

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

// ---------------------------------------------------------------- 假地图底图

@Composable
private fun FakeMap(modifier: Modifier = Modifier) {
    val grid = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    val resColor = Color(0xFF6BB5FF)
    val enlColor = Color(0xFF4EDE92)
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF0E1113), Color(0xFF0B0E10))
            )
        )
        val step = 64.dp.toPx()
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
        // 假 links
        val p1 = Offset(size.width * 0.18f, size.height * 0.22f)
        val p2 = Offset(size.width * 0.76f, size.height * 0.18f)
        val p3 = Offset(size.width * 0.32f, size.height * 0.62f)
        drawLine(enlColor.copy(alpha = 0.5f), p1, p2, 2.dp.toPx())
        drawLine(resColor.copy(alpha = 0.5f), p1, p3, 2.dp.toPx())
        // 假 portal
        listOf(
            p1 to enlColor,
            p2 to resColor,
            p3 to resColor,
            Offset(size.width * 0.7f, size.height * 0.72f) to Color(0xFFE5C158)
        ).forEach { (center, color) ->
            drawCircle(color.copy(alpha = 0.16f), radius = 34.dp.toPx(), center = center)
            drawCircle(color, radius = 5.dp.toPx(), center = center)
        }
    }
}

// ---------------------------------------------------------------- Portal peek bar（布局参照）

@Composable
private fun FakePeekBar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 6.dp,
        shadowElevation = 12.dp
    ) {
        Column(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF004785), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text("L6", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCCE5FF))
                        }
                        Text(
                            "78%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Azure Reserve Memorial",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(8) { i ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(5.dp)
                                    .background(
                                        if (i < 6) Color(0xFF6BB5FF) else Color.Transparent,
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- 需求 1 & 2：地图安全区

@Composable
private fun MapSafeAreaScaffold(
    avoidSystemBars: Boolean,
    content: @Composable BoxScope.() -> Unit
) {
    val statusBar = if (avoidSystemBars) PREVIEW_STATUSBAR_DP.dp else 0.dp
    val navBar = if (avoidSystemBars) PREVIEW_NAVBAR_DP.dp else 0.dp
    Box(modifier = Modifier.fillMaxSize()) {
        // 地图区：真机上是 windowInsetsPadding(statusBars) + windowInsetsPadding(navigationBars)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBar, bottom = navBar),
            content = content
        )

        if (avoidSystemBars) {
            // 状态栏实色表面 + 0.5dp 分割线
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBar)
                    .align(Alignment.TopCenter)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                verticalArrangement = Arrangement.Bottom
            ) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            }
            // 导航栏实色表面，与地图下边缘相邻
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(navBar)
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            )
        }
    }
}

@Preview(name = "5 · 选中 portal：FAB 随 sheet 上移", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
private fun FabLiftWithSheetPreview() {
    PreviewTheme {
        MapSafeAreaScaffold(avoidSystemBars = true) {
            FakeMap(Modifier.fillMaxSize())
            FakePeekBar(Modifier.align(Alignment.BottomCenter))
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
                // 与生产代码同源：sheet peek 高度 + 间距
                bottomOffset = PortalSheetDefaults.PeekHeight + PortalSheetDefaults.FabGap,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(name = "5 · 未选中 portal：FAB 贴底", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
private fun FabIdlePreview() {
    PreviewTheme {
        MapSafeAreaScaffold(avoidSystemBars = true) {
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

@Preview(name = "1+2 · 地图安全区（改后）", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
private fun MapSafeAreaAfterPreview() {
    PreviewTheme {
        MapSafeAreaScaffold(avoidSystemBars = true) {
            FakeMap(Modifier.fillMaxSize())
            FakePeekBar(Modifier.align(Alignment.BottomCenter))
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

@Preview(name = "1+2 · 地图铺满（改前）", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
private fun MapSafeAreaBeforePreview() {
    PreviewTheme {
        MapSafeAreaScaffold(avoidSystemBars = false) {
            FakeMap(Modifier.fillMaxSize())
            FakePeekBar(Modifier.align(Alignment.BottomCenter))
        }
    }
}

/** 顶部局部放大：看状态栏与地图的分隔线 */
@Preview(name = "2 · 状态栏分隔（局部）", widthDp = 411, heightDp = 180, showBackground = true)
@Composable
private fun StatusBarSeparatorPreview() {
    PreviewTheme {
        MapSafeAreaScaffold(avoidSystemBars = true) {
            FakeMap(Modifier.fillMaxSize())
        }
    }
}

// ---------------------------------------------------------------- 需求 3：Mods 卡片

@Preview(name = "3 · Mods 卡片（改后：仅名称）", widthDp = 411, showBackground = true)
@Composable
private fun ModsRowAfterPreview() {
    PreviewTheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PortalModsHeader()
            PortalModsRow(mods = previewMods, onModClick = {})
        }
    }
}

@Preview(name = "3 · Mods 卡片（改前：带稀有度行）", widthDp = 411, showBackground = true)
@Composable
private fun ModsRowBeforePreview() {
    PreviewTheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PortalModsHeader()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                previewMods.forEach { mod ->
                    LegacyModSlotCard(mod, Modifier.weight(1f))
                }
                LegacyModSlotCard(null, Modifier.weight(1f))
            }
        }
    }
}

/** 改前样式的复刻，仅用于对比 */
@Composable
private fun LegacyModSlotCard(mod: PortalMod?, modifier: Modifier = Modifier) {
    androidx.compose.material3.Card(
        modifier = modifier.height(68.dp),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (mod != null) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (mod != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = mod.name.orEmpty(),
                        color = mod.rarityColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = mod.rarity.orEmpty(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            } else {
                Text(
                    text = "Empty",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Preview(name = "3 · Mod 详情弹窗", widthDp = 411, heightDp = 640, showBackground = true)
@Composable
private fun ModDetailDialogPreview() {
    PreviewTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            PortalModDetailDialog(mod = previewMods.first(), onDismiss = {})
        }
    }
}

// ---------------------------------------------------------------- 需求 4：FAB Menu

@Preview(name = "4 · FAB Menu 收起", widthDp = 411, heightDp = 400, showBackground = true)
@Composable
private fun FabMenuCollapsedPreview() {
    PreviewTheme {
        Box(Modifier.fillMaxSize()) {
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
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(name = "4 · FAB Menu 展开", widthDp = 411, heightDp = 640, showBackground = true)
@Composable
private fun FabMenuExpandedPreview() {
    PreviewTheme {
        Box(Modifier.fillMaxSize()) {
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
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
