package io.github.movetakeitblue.iitcexpressive.ui.menu

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.movetakeitblue.iitcexpressive.i18n.L

/**
 * 单个 FAB Menu 动作项。
 */
private data class FabMenuAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

/**
 * Material 3 Expressive FAB Menu 与 Floating Toolbar with FAB
 *
 * 规格对齐 M3 Expressive 规范：
 * - 当 [persistentToolbar] = true 时：
 *   在地图底部常驻展示 M3E 风格的 Floating Toolbar，常驻提供 Settings, Locate, Reload 按钮与末端 FAB。
 *   点击末端 FAB 仅在上方展开其余快捷操作（Layers, Chat, Bookmarks, Portal List, Draw Tools，以及开启时的 Debug）。
 * - 当 [persistentToolbar] = false 时：
 *   保持标准的 56dp FAB，点击展开全部快捷菜单项。
 * - Plugins 入口统一由 Settings 进入，不在此处显示。
 * - Debug 仅在 [showDebug] 为 true（用户在 Settings 中开启）时才显示。
 */
@Composable
fun FabMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlugins: () -> Unit = {},
    onOpenLayers: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenBookmarks: () -> Unit = {},
    onOpenPortalList: () -> Unit = {},
    onOpenDrawTools: () -> Unit = {},
    onLocate: () -> Unit,
    onDebug: () -> Unit,
    onReload: () -> Unit,
    isIitcLoaded: Boolean = true,
    showDebug: Boolean = false,
    persistentToolbar: Boolean = false,
    bottomOffset: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    // 「+」旋转 45° 即为「×」，符合 M3 FAB menu 的图标变换约定
    val iconRotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fabIconRotation"
    )

    // 与 Portal 详情 sheet 同步弹簧参数，随 sheet 展开平滑上浮
    val animatedBottom by animateDpAsState(
        targetValue = bottomOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fabBottomOffset"
    )

    // 弹出的菜单项列表
    val actions = remember(
        isIitcLoaded,
        showDebug,
        persistentToolbar,
        onNavigateToSettings,
        onOpenLayers,
        onOpenChat,
        onOpenBookmarks,
        onOpenPortalList,
        onOpenDrawTools,
        onLocate,
        onDebug,
        onReload
    ) {
        if (persistentToolbar) {
            // 常驻工具栏模式：Settings, Locate, Reload 已常驻在 toolbar 上
            // FAB 展开仅显示其余项
            if (!isIitcLoaded) {
                emptyList()
            } else {
                buildList {
                    add(FabMenuAction(Icons.Default.Layers, L("panel.layers"), onOpenLayers))
                    add(FabMenuAction(Icons.AutoMirrored.Filled.Chat, L("nav.chat"), onOpenChat))
                    add(FabMenuAction(Icons.Default.Bookmarks, "Bookmarks", onOpenBookmarks))
                    add(FabMenuAction(Icons.AutoMirrored.Filled.FormatListBulleted, "Portal List", onOpenPortalList))
                    add(FabMenuAction(Icons.Default.Gesture, "Draw Tools", onOpenDrawTools))
                    if (showDebug) {
                        add(FabMenuAction(Icons.Default.Terminal, L("nav.debug"), onDebug))
                    }
                }
            }
        } else {
            // 单 FAB 模式：点击展开所有功能（Plugins 移除，Debug 受设置控制）
            if (!isIitcLoaded) {
                buildList {
                    add(FabMenuAction(Icons.Default.Settings, L("nav.settings"), onNavigateToSettings))
                    if (showDebug) {
                        add(FabMenuAction(Icons.Default.Terminal, L("nav.debug"), onDebug))
                    }
                    add(FabMenuAction(Icons.Default.Refresh, L("nav.reload_iitc"), onReload))
                }
            } else {
                buildList {
                    add(FabMenuAction(Icons.Default.Settings, L("nav.settings"), onNavigateToSettings))
                    add(FabMenuAction(Icons.Default.Layers, L("panel.layers"), onOpenLayers))
                    add(FabMenuAction(Icons.AutoMirrored.Filled.Chat, L("nav.chat"), onOpenChat))
                    add(FabMenuAction(Icons.Default.Bookmarks, "Bookmarks", onOpenBookmarks))
                    add(FabMenuAction(Icons.AutoMirrored.Filled.FormatListBulleted, "Portal List", onOpenPortalList))
                    add(FabMenuAction(Icons.Default.Gesture, "Draw Tools", onOpenDrawTools))
                    add(FabMenuAction(Icons.Default.MyLocation, L("panel.locate"), onLocate))
                    if (showDebug) {
                        add(FabMenuAction(Icons.Default.Terminal, L("nav.debug"), onDebug))
                    }
                    add(FabMenuAction(Icons.Default.Refresh, L("nav.reload_iitc"), onReload))
                }
            }
        }
    }

    Box(modifier = modifier) {
        // 透明点击层：点击空白处收起菜单
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(80))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggle
                    )
            )
        }

        val columnAlignment = if (persistentToolbar) Alignment.BottomCenter else Alignment.BottomEnd
        val columnHorizontalAlignment = Alignment.End
        val columnPadding = if (persistentToolbar) {
            Modifier.padding(bottom = animatedBottom)
        } else {
            Modifier.padding(end = 16.dp, bottom = animatedBottom)
        }

        Column(
            modifier = Modifier
                .align(columnAlignment)
                .navigationBarsPadding()
                .then(columnPadding),
            horizontalAlignment = columnHorizontalAlignment,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 展开的快捷功能项
            actions.forEachIndexed { index, action ->
                val enterDelay = index * 22
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(120, delayMillis = enterDelay)) +
                        scaleIn(
                            initialScale = 0.85f,
                            animationSpec = tween(160, delayMillis = enterDelay)
                        ),
                    exit = fadeOut(tween(80)) + scaleOut(targetScale = 0.9f, animationSpec = tween(90))
                ) {
                    FabMenuItem(
                        icon = action.icon,
                        label = action.label,
                        onClick = {
                            onToggle()
                            action.onClick()
                        }
                    )
                }
            }

            // 底部触发区域：根据 persistentToolbar 渲染 Floating Toolbar 或 单 FAB
            if (persistentToolbar) {
                // M3 Expressive Floating Toolbar with Separated FAB
                // 胶囊形工具栏与独立方圆角 FAB 分离并列居中展示
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // 1. 胶囊形状常驻工具栏
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 6.dp,
                        shadowElevation = 6.dp,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(
                                horizontal = 6.dp,
                                vertical = 4.dp
                            )
                        ) {
                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = L("nav.settings"),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            if (isIitcLoaded) {
                                IconButton(
                                    onClick = onLocate,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = L("panel.locate"),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onReload,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = L("nav.reload_iitc"),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // 2. 独立分离出来的 M3 Expressive FAB 按钮（圆角矩形）
                    if (isIitcLoaded) {
                        FloatingActionButton(
                            onClick = onToggle,
                            shape = RoundedCornerShape(18.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 6.dp,
                                focusedElevation = 6.dp,
                                hoveredElevation = 6.dp
                            ),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = if (isExpanded) "Close menu" else "Open menu",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .size(26.dp)
                                    .rotate(iconRotation)
                            )
                        }
                    }
                }
            } else {
                // 标准单 FAB（56dp 圆形）
                FloatingActionButton(
                    onClick = onToggle,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isExpanded) "Close menu" else "Open menu",
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(iconRotation)
                    )
                }
            }
        }
    }
}

/**
 * M3 Expressive FAB menu item：44dp 高胶囊，图标 + 文字，无阴影。
 */
@Composable
private fun FabMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
