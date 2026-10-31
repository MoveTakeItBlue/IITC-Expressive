package io.github.movetakeitblue.iitcexpressive.plugin.ui.farmsfind

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.movetakeitblue.iitcexpressive.model.NativePortal
import io.github.movetakeitblue.iitcexpressive.plugin.api.*
import io.github.movetakeitblue.iitcexpressive.theme.*
import kotlin.math.*

data class FarmCluster(
    val id: String,
    val centerLat: Double,
    val centerLng: Double,
    val radiusMeters: Double,
    val primaryName: String,
    val portals: List<NativePortal>,
    val team: Int, // 1: Res, 2: Enl, 3: Mac, 0: Neutral, -1: Mixed
    val avgLevel: Double,
    val levelCounts: Map<Int, Int>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmsFindSheet(
    host: IitcPluginHost,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val portals by host.visiblePortals.collectAsState()
    val drawnShapes by host.drawnShapes.collectAsState()

    var minLevel by remember { mutableStateOf(7) }
    var minCount by remember { mutableStateOf(3) }
    var factionFilter by remember { mutableStateOf(-1) } // -1: All, 1: Res, 2: Enl, 3: Mac
    var clusterRadiusMeters by remember { mutableStateOf(300.0) }
    var expandedFarmId by remember { mutableStateOf<String?>(null) }

    // Compute clusters
    val clusters = remember(portals, minLevel, minCount, factionFilter, clusterRadiusMeters) {
        findFarmClusters(
            portals = portals,
            minLevel = minLevel,
            minCount = minCount,
            factionFilter = factionFilter,
            clusterRadiusMeters = clusterRadiusMeters
        )
    }

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
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Find Farms",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${clusters.size} Farm Clusters • ${portals.size} Portals in View",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Filter Controls
            Text(
                text = "MINIMUM PORTAL LEVEL",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            ) {
                listOf(5 to "L5+", 6 to "L6+", 7 to "L7+", 8 to "L8").forEach { (lvl, label) ->
                    item {
                        FilterChip(
                            selected = minLevel == lvl,
                            onClick = { minLevel = lvl },
                            label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Min portal count
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MIN PORTALS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(3, 5, 8).forEach { count ->
                            FilterChip(
                                selected = minCount == count,
                                onClick = { minCount = count },
                                label = { Text("$count+", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Faction filter
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = "FACTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(-1 to "All", 1 to "RES", 2 to "ENL", 3 to "MAC").forEach { (fac, label) ->
                            FilterChip(
                                selected = factionFilter == fac,
                                onClick = { factionFilter = fac },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Farm clusters list
            if (clusters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No farms matching criteria in current view",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Try lowering minimum level or count, or pan the map",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(clusters, key = { it.id }) { farm ->
                        FarmCard(
                            farm = farm,
                            isExpanded = expandedFarmId == farm.id,
                            onToggleExpand = {
                                expandedFarmId = if (expandedFarmId == farm.id) null else farm.id
                            },
                            onFlyTo = {
                                host.flyTo(farm.centerLat, farm.centerLng, zoom = 16.5)
                                onDismiss()
                            },
                            onAddBoundary = {
                                val boundaryCircle = DrawnShape.Circle(
                                    id = "farm_${farm.id}",
                                    color = when (farm.team) {
                                        1 -> "#004785"
                                        2 -> "#00522B"
                                        3 -> "#680009"
                                        else -> "#a24ac3"
                                    },
                                    center = ShapeLatLng(farm.centerLat, farm.centerLng),
                                    radiusMeters = farm.radiusMeters.coerceAtLeast(100.0)
                                )
                                host.saveDrawnShapes(drawnShapes + boundaryCircle)
                                host.showToast("Farm boundary added to Draw Tools")
                            },
                            onSelectPortal = { portal ->
                                host.flyTo(portal.lat, portal.lng, zoom = 17.5)
                                host.selectPortal(portal.guid)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FarmCard(
    farm: FarmCluster,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onFlyTo: () -> Unit,
    onAddBoundary: () -> Unit,
    onSelectPortal: (NativePortal) -> Unit
) {
    val factionColor = when (farm.team) {
        1 -> TeamResistance
        2 -> TeamEnlightened
        3 -> TeamMachina
        0 -> TeamNeutral
        else -> MaterialTheme.colorScheme.primary
    }

    val factionBg = when (farm.team) {
        1 -> TeamResistanceContainer
        2 -> TeamEnlightenedContainer
        3 -> TeamMachinaContainer
        0 -> TeamNeutralContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Title & Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(factionColor)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = farm.primaryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = factionBg,
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Text(
                        text = "Avg L${((farm.avgLevel * 10).roundToInt() / 10.0)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = factionColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${farm.portals.size} Portals",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Radius: ${farm.radiusMeters.roundToInt()}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Level distribution pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                farm.levelCounts.entries.sortedByDescending { it.key }.forEach { (lvl, count) ->
                    item {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "${count}× L$lvl",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onFlyTo,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Fly To", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onAddBoundary,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Icon(imageVector = Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Draw Boundary", fontSize = 12.sp)
                }

                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Portals",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded individual portals list
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(6.dp))
                    farm.portals.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectPortal(p) }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${p.level}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = p.title.ifBlank { "Portal" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Spatial clustering algorithm to find farm clusters.
 */
private fun findFarmClusters(
    portals: List<NativePortal>,
    minLevel: Int,
    minCount: Int,
    factionFilter: Int,
    clusterRadiusMeters: Double
): List<FarmCluster> {
    val candidates = portals.filter { p ->
        p.level >= minLevel && (factionFilter == -1 || p.team == factionFilter)
    }
    if (candidates.isEmpty()) return emptyList()

    val visited = BooleanArray(candidates.size)
    val clusters = mutableListOf<FarmCluster>()

    for (i in candidates.indices) {
        if (visited[i]) continue
        visited[i] = true

        val clusterPortals = mutableListOf(candidates[i])
        val queue = ArrayDeque<NativePortal>()
        queue.add(candidates[i])

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            for (j in candidates.indices) {
                if (!visited[j]) {
                    val other = candidates[j]
                    val dist = haversineMeters(curr.lat, curr.lng, other.lat, other.lng)
                    if (dist <= clusterRadiusMeters) {
                        visited[j] = true
                        clusterPortals.add(other)
                        queue.add(other)
                    }
                }
            }
        }

        if (clusterPortals.size >= minCount) {
            val centerLat = clusterPortals.map { it.lat }.average()
            val centerLng = clusterPortals.map { it.lng }.average()
            var maxDist = 0.0
            for (p in clusterPortals) {
                val d = haversineMeters(centerLat, centerLng, p.lat, p.lng)
                if (d > maxDist) maxDist = d
            }

            val teams = clusterPortals.map { it.team }.distinct()
            val clusterTeam = if (teams.size == 1) teams.first() else -1
            val avgLvl = clusterPortals.map { it.level }.average()
            val lvlCounts = clusterPortals.groupBy { it.level }.mapValues { it.value.size }

            val primary = clusterPortals.maxByOrNull { it.level }?.title?.ifBlank { "Farm Cluster" } ?: "Farm Cluster"

            clusters.add(
                FarmCluster(
                    id = "farm_${centerLat}_${centerLng}",
                    centerLat = centerLat,
                    centerLng = centerLng,
                    radiusMeters = maxDist + 30.0,
                    primaryName = primary,
                    portals = clusterPortals.sortedByDescending { it.level },
                    team = clusterTeam,
                    avgLevel = avgLvl,
                    levelCounts = lvlCounts
                )
            )
        }
    }

    return clusters.sortedByDescending { it.portals.size * 10 + it.avgLevel }
}

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0 // Earth radius in meters
    val dLat = (lat2 - lat1) * PI / 180.0
    val dLon = (lon2 - lon1) * PI / 180.0
    val a = sin(dLat / 2).pow(2) + cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
