package io.github.movetakeitblue.iitcexpressive.ui.portal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.movetakeitblue.iitcexpressive.model.PortalMod

/**
 * Mods 槽位行（4 个卡片）。
 *
 * 无状态、不依赖 ViewModel，便于在 Compose Preview 中直接渲染。
 * 卡片只展示 mod 名称（颜色按稀有度），稀有度 / 部署 agent / stats 统一在点击后的
 * [PortalModDetailDialog] 中展示。
 */
@Composable
fun PortalModsRow(
    mods: List<PortalMod>?,
    onModClick: (PortalMod) -> Unit,
    modifier: Modifier = Modifier
) {
    val slots = mods ?: listOf(PortalMod(), PortalMod(), PortalMod(), PortalMod())
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0..3) {
            val clickableMod = slots.getOrNull(i)?.takeIf { !it.name.isNullOrBlank() }
            ModSlotCard(
                mod = clickableMod,
                onClick = { clickableMod?.let(onModClick) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModSlotCard(
    mod: PortalMod?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasMod = mod != null
    Card(
        modifier = modifier
            .height(60.dp)
            .clickable(enabled = hasMod, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasMod) {
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
                Text(
                    text = mod.name.orEmpty(),
                    color = mod.rarityColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
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

/**
 * Mods 区块标题（MODS + Tap to inspect）。
 */
@Composable
fun PortalModsHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "MODS",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Tap to inspect",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontSize = 11.sp
        )
    }
}

/**
 * 点击 mod 卡片后的详情弹窗：稀有度 chip + 部署 agent + stats。
 */
@Composable
fun PortalModDetailDialog(
    mod: PortalMod,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        icon = {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = mod.rarityColor,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = mod.name ?: "Mod Detail",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!mod.rarity.isNullOrBlank()) {
                    Surface(
                        color = mod.rarityColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = mod.rarity,
                            color = mod.rarityColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (!mod.owner.isNullOrBlank()) {
                            "Deployed by: ${mod.owner}"
                        } else {
                            "Deployed by: Unknown Agent"
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                val stats = mod.stats
                if (!stats.isNullOrEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        stats.forEach { (k, v) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = k,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = v,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
