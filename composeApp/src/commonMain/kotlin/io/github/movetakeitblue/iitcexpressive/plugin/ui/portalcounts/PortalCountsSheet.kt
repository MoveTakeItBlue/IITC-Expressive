package io.github.movetakeitblue.iitcexpressive.plugin.ui.portalcounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.movetakeitblue.iitcexpressive.model.NativePortal
import io.github.movetakeitblue.iitcexpressive.plugin.api.CycleTimesCalculator
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.theme.*

class FactionPortalCount {
    var totalPortals: Int = 0
    var totalResonators: Int = 0
    var totalEnergy: Long = 0
    val levelCounts = IntArray(9) // index 0..8
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalCountsSheet(
    host: IitcPluginHost,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val portals by host.visiblePortals.collectAsState()
    val links by host.visibleLinks.collectAsState()
    val fields by host.visibleFields.collectAsState()

    val resCounts = remember(portals) { calculateFactionCount(portals, 1) }
    val enlCounts = remember(portals) { calculateFactionCount(portals, 2) }
    val macCounts = remember(portals) { calculateFactionCount(portals, 3) }
    val neutralCounts = remember(portals) { calculateFactionCount(portals, 0) }

    val totalPortals = portals.size.coerceAtLeast(1)
    val resRatio = resCounts.totalPortals.toFloat() / totalPortals.toFloat()
    val enlRatio = enlCounts.totalPortals.toFloat() / totalPortals.toFloat()
    val macRatio = macCounts.totalPortals.toFloat() / totalPortals.toFloat()
    val neutralRatio = neutralCounts.totalPortals.toFloat() / totalPortals.toFloat()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Portal Counts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$totalPortals Portals • ${links.size} Links • ${fields.size} Fields",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Multi-faction ratio bar
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "FACTION DISTRIBUTION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            ) {
                                if (resRatio > 0) Box(modifier = Modifier.fillMaxHeight().weight(resRatio).background(ResBlue))
                                if (enlRatio > 0) Box(modifier = Modifier.fillMaxHeight().weight(enlRatio).background(EnlGreen))
                                if (macRatio > 0) Box(modifier = Modifier.fillMaxHeight().weight(macRatio).background(MacRed))
                                if (neutralRatio > 0) Box(modifier = Modifier.fillMaxHeight().weight(neutralRatio).background(NeutralOrange))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                LegendItem("RES", resCounts.totalPortals, ResBlue)
                                LegendItem("ENL", enlCounts.totalPortals, EnlGreen)
                                LegendItem("MAC", macCounts.totalPortals, MacRed)
                                LegendItem("NEU", neutralCounts.totalPortals, NeutralOrange)
                            }
                        }
                    }
                }

                // Level Distribution Grid
                item {
                    Text(
                        text = "LEVEL BREAKDOWN (L1 - L8)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FactionLevelRow("Resistance", resCounts.levelCounts, ResBlue)
                            FactionLevelRow("Enlightened", enlCounts.levelCounts, EnlGreen)
                            FactionLevelRow("Machina", macCounts.levelCounts, MacRed)
                        }
                    }
                }

                // Entity & Energy Metrics
                item {
                    Text(
                        text = "TOTAL ENERGY & ENTITIES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Resonators",
                            count = "${resCounts.totalResonators + enlCounts.totalResonators + macCounts.totalResonators}",
                            icon = Icons.Default.Bolt,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Links",
                            count = "${links.size}",
                            icon = Icons.Default.Share,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Fields",
                            count = "${fields.size}",
                            icon = Icons.Default.ChangeHistory,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text = "$label: $count", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun FactionLevelRow(
    factionName: String,
    levels: IntArray,
    color: androidx.compose.ui.graphics.Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = factionName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (lvl in 1..8) {
                val count = levels[lvl]
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (count > 0) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "L$lvl",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (count > 0) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun calculateFactionCount(portals: List<NativePortal>, targetTeam: Int): FactionPortalCount {
    val count = FactionPortalCount()
    portals.filter { it.team == targetTeam }.forEach { p ->
        count.totalPortals++
        val res = p.resCount.coerceIn(0, 8)
        count.totalResonators += res
        val lvl = p.level.coerceIn(1, 8)
        count.levelCounts[lvl]++
    }
    return count
}
