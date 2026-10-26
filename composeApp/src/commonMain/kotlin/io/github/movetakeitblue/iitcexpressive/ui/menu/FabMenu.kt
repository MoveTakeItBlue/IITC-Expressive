package io.github.movetakeitblue.iitcexpressive.ui.menu

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
 * Material 3 Expressive FAB Menu（默认紧凑样式）
 *
 * 规格对齐 M3 Expressive FAB menu：
 * - 触发 FAB：56dp 圆形，primaryContainer；图标旋转 45° 从「+」变成「×」，不替换图标。
 * - 菜单项：40dp 高的 pill（胶囊），secondaryContainer，无阴影/无描边，图标 18dp + 12sp Medium 文字。
 * - 间距：菜单项之间 6dp，最后一项与触发 FAB 之间 12dp。
 * - 展开时逐项错峰淡入 + 轻微缩放；不使用厚重遮罩，只保留一层透明点击层用于点击空白关闭。
 */
@Composable
fun FabMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    onOpenLayers: () -> Unit,
    onOpenChat: () -> Unit,
    onLocate: () -> Unit,
    onDebug: () -> Unit,
    onReload: () -> Unit,
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

    // 与 Portal 详情 sheet 使用同一套弹簧参数，保证 FAB 与 sheet 同步上浮、不会错位
    val animatedBottom by animateDpAsState(
        targetValue = bottomOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fabBottomOffset"
    )

    val actions = listOf(
        FabMenuAction(Icons.Default.Settings, L("nav.settings"), onNavigateToSettings),
        FabMenuAction(Icons.Default.Extension, L("nav.plugins"), onNavigateToPlugins),
        FabMenuAction(Icons.Default.Layers, L("panel.layers"), onOpenLayers),
        FabMenuAction(Icons.AutoMirrored.Filled.Chat, L("nav.chat"), onOpenChat),
        FabMenuAction(Icons.Default.MyLocation, L("panel.locate"), onLocate),
        FabMenuAction(Icons.Default.Terminal, L("nav.debug"), onDebug),
        FabMenuAction(Icons.Default.Refresh, L("nav.reload_iitc"), onReload)
    )

    Box(modifier = modifier) {
        // 透明点击层：保留「点击空白处收起」，但不额外压暗地图
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

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = animatedBottom),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
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
                        onClick = action.onClick
                    )
                }
            }

            // 触发 FAB：56dp，颜色在展开/收起时保持一致（M3 默认行为）
            // 顶部 6dp padding 与 Column 的 6dp 间距叠加 = 与菜单项间隔 12dp；收起时无菜单项，不影响 FAB 位置
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

/**
 * M3 Expressive FAB menu item：40dp 高胶囊，图标 + 文字，无阴影。
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
        modifier = modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
