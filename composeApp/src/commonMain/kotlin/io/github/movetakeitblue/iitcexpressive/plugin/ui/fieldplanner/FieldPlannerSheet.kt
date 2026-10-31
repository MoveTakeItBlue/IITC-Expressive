package io.github.movetakeitblue.iitcexpressive.plugin.ui.fieldplanner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import io.github.movetakeitblue.iitcexpressive.model.NativeLink
import io.github.movetakeitblue.iitcexpressive.model.NativePortal
import io.github.movetakeitblue.iitcexpressive.plugin.api.*
import io.github.movetakeitblue.iitcexpressive.theme.*
import kotlin.math.*

enum class PlannerMode {
    FLY_LINKS,   // Longest links first
    TIDY_LINKS   // Shortest links first
}

data class PlannedLink(
    val id: String,
    val fromPortal: NativePortal,
    val toPortal: NativePortal,
    val distanceMeters: Double
)

data class FieldPlanResult(
    val plannedLinks: List<PlannedLink>,
    val estimatedFieldsCount: Int,
    val totalDistanceMeters: Double,
    val totalAp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldPlannerSheet(
    host: IitcPluginHost,
    initialMode: PlannerMode = PlannerMode.FLY_LINKS,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val portals by host.visiblePortals.collectAsState()
    val activeLinks by host.visibleLinks.collectAsState()
    val drawnShapes by host.drawnShapes.collectAsState()

    var plannerMode by remember { mutableStateOf(initialMode) }
    var factionFilter by remember { mutableStateOf(-1) } // -1: All, 1: Res, 2: Enl, 0: Neutral
    var avoidExistingLinks by remember { mutableStateOf(true) }
    var planColor by remember { mutableStateOf("#a24ac3") }

    // Calculate plan
    val planResult = remember(portals, activeLinks, plannerMode, factionFilter, avoidExistingLinks) {
        computeFieldPlan(
            portals = portals,
            existingLinks = if (avoidExistingLinks) activeLinks else emptyList(),
            mode = plannerMode,
            factionFilter = factionFilter
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
                            imageVector = if (plannerMode == PlannerMode.FLY_LINKS) Icons.Default.AutoAwesome else Icons.Default.Polyline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (plannerMode == PlannerMode.FLY_LINKS) "Fly Links Planner" else "Tidy Links Planner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${planResult.plannedLinks.size} Links • ~${planResult.estimatedFieldsCount} Fields • ${(planResult.totalDistanceMeters / 1000.0).roundToInt()} km",
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

            // Strategy Segmented Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (plannerMode == PlannerMode.FLY_LINKS) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { plannerMode = PlannerMode.FLY_LINKS }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (plannerMode == PlannerMode.FLY_LINKS) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fly Links (Longest)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (plannerMode == PlannerMode.FLY_LINKS) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (plannerMode == PlannerMode.TIDY_LINKS) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { plannerMode = PlannerMode.TIDY_LINKS }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Polyline,
                            contentDescription = null,
                            tint = if (plannerMode == PlannerMode.TIDY_LINKS) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tidy Links (Shortest)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (plannerMode == PlannerMode.TIDY_LINKS) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    listOf(-1 to "All Portals", 1 to "RES Only", 2 to "ENL Only").forEach { (fKey, label) ->
                        item {
                            FilterChip(
                                selected = factionFilter == fKey,
                                onClick = { factionFilter = fKey },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                    item {
                        FilterChip(
                            selected = avoidExistingLinks,
                            onClick = { avoidExistingLinks = !avoidExistingLinks },
                            label = { Text("Avoid Cross-Links", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${planResult.plannedLinks.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Links", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${planResult.estimatedFieldsCount}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text("Fields", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(planResult.totalDistanceMeters / 1000.0).roundToInt()} km",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text("Length", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "+${planResult.totalAp}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEAA000)
                        )
                        Text("Est. AP", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val newPolylines = planResult.plannedLinks.mapIndexed { idx, link ->
                            DrawnShape.Polyline(
                                id = "plan_link_${idx}_${link.id}",
                                color = planColor,
                                latLngs = listOf(
                                    ShapeLatLng(link.fromPortal.lat, link.fromPortal.lng),
                                    ShapeLatLng(link.toPortal.lat, link.toPortal.lng)
                                )
                            )
                        }
                        val cleanedShapes = drawnShapes.filterNot { it.id.startsWith("plan_link_") }
                        host.saveDrawnShapes(cleanedShapes + newPolylines)
                        host.showToast("${newPolylines.size} plan links added to Draw Tools")
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add to Draw Tools", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val cleanedShapes = drawnShapes.filterNot { it.id.startsWith("plan_link_") }
                        host.saveDrawnShapes(cleanedShapes)
                        host.showToast("Plan links removed from Draw Tools")
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Plan", fontSize = 12.sp)
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Step-by-step links list
            if (planResult.plannedLinks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No candidate links found in view",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Zoom out to include at least 3 portals",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    itemsIndexed(planResult.plannedLinks, key = { _, it -> it.id }) { index, link ->
                        PlanLinkItemCard(
                            step = index + 1,
                            link = link,
                            onFlyToOrigin = {
                                host.flyTo(link.fromPortal.lat, link.fromPortal.lng, zoom = 17.0)
                                host.selectPortal(link.fromPortal.guid)
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
private fun PlanLinkItemCard(
    step: Int,
    link: PlannedLink,
    onFlyToOrigin: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFlyToOrigin() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$step",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = link.fromPortal.title.ifBlank { "Portal" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(12.dp).padding(horizontal = 2.dp)
                    )
                    Text(
                        text = link.toPortal.title.ifBlank { "Portal" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "${(link.distanceMeters / 1000.0 * 10).roundToInt() / 10.0} km",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Computes non-intersecting triangulation plan for candidate portals.
 */
private fun computeFieldPlan(
    portals: List<NativePortal>,
    existingLinks: List<NativeLink>,
    mode: PlannerMode,
    factionFilter: Int
): FieldPlanResult {
    val candidates = portals.filter { p ->
        factionFilter == -1 || p.team == factionFilter
    }
    if (candidates.size < 2) {
        return FieldPlanResult(emptyList(), 0, 0.0, 0L)
    }

    // Limit to 40 portals in view for fast responsive client-side calculation
    val limitedPortals = candidates.take(40)

    // Generate all pairs
    data class CandidateEdge(
        val p1: NativePortal,
        val p2: NativePortal,
        val dist: Double
    )

    val allEdges = mutableListOf<CandidateEdge>()
    for (i in 0 until limitedPortals.size) {
        for (j in i + 1 until limitedPortals.size) {
            val p1 = limitedPortals[i]
            val p2 = limitedPortals[j]
            val d = haversineMeters(p1.lat, p1.lng, p2.lat, p2.lng)
            allEdges.add(CandidateEdge(p1, p2, d))
        }
    }

    // Sort edges: Fly Links = Longest first; Tidy Links = Shortest first
    when (mode) {
        PlannerMode.FLY_LINKS -> allEdges.sortByDescending { it.dist }
        PlannerMode.TIDY_LINKS -> allEdges.sortBy { it.dist }
    }

    val selectedEdges = mutableListOf<CandidateEdge>()

    // Greedy planar embedding
    for (edge in allEdges) {
        var intersects = false

        // Check intersection with already selected plan edges
        for (sel in selectedEdges) {
            if (segmentsIntersect(
                edge.p1.lat, edge.p1.lng, edge.p2.lat, edge.p2.lng,
                sel.p1.lat, sel.p1.lng, sel.p2.lat, sel.p2.lng
            )) {
                intersects = true
                break
            }
        }

        // Check intersection with active map links
        if (!intersects && existingLinks.isNotEmpty()) {
            for (act in existingLinks) {
                if (segmentsIntersect(
                    edge.p1.lat, edge.p1.lng, edge.p2.lat, edge.p2.lng,
                    act.oLat, act.oLng, act.dLat, act.dLng
                )) {
                    intersects = true
                    break
                }
            }
        }

        if (!intersects) {
            selectedEdges.add(edge)
        }
    }

    // Detect 3-cycles (triangular fields)
    val adj = mutableMapOf<String, MutableSet<String>>()
    for (edge in selectedEdges) {
        adj.getOrPut(edge.p1.guid) { mutableSetOf() }.add(edge.p2.guid)
        adj.getOrPut(edge.p2.guid) { mutableSetOf() }.add(edge.p1.guid)
    }

    var fieldsCount = 0
    val portalGuidMap = limitedPortals.associateBy { it.guid }
    val visitedTriangles = mutableSetOf<String>()

    for (edge in selectedEdges) {
        val neighbors1 = adj[edge.p1.guid] ?: emptySet()
        val neighbors2 = adj[edge.p2.guid] ?: emptySet()
        val common = neighbors1.intersect(neighbors2)

        for (cGuid in common) {
            val triKey = listOf(edge.p1.guid, edge.p2.guid, cGuid).sorted().joinToString("_")
            if (visitedTriangles.add(triKey)) {
                fieldsCount++
            }
        }
    }

    val plannedLinks = selectedEdges.mapIndexed { idx, e ->
        PlannedLink(
            id = "${e.p1.guid}_${e.p2.guid}",
            fromPortal = e.p1,
            toPortal = e.p2,
            distanceMeters = e.dist
        )
    }

    val totalDist = plannedLinks.sumOf { it.distanceMeters }
    val totalAp = (plannedLinks.size * 800L) + (fieldsCount * 1563L)

    return FieldPlanResult(
        plannedLinks = plannedLinks,
        estimatedFieldsCount = fieldsCount,
        totalDistanceMeters = totalDist,
        totalAp = totalAp
    )
}

/**
 * Proper segment intersection test (excludes touching endpoints).
 */
private fun segmentsIntersect(
    p0Lat: Double, p0Lng: Double, p1Lat: Double, p1Lng: Double,
    p2Lat: Double, p2Lng: Double, p3Lat: Double, p3Lng: Double
): Boolean {
    // Shared endpoints do not intersect
    if ((p0Lat == p2Lat && p0Lng == p2Lng) || (p0Lat == p3Lat && p0Lng == p3Lng) ||
        (p1Lat == p2Lat && p1Lng == p2Lng) || (p1Lat == p3Lat && p1Lng == p3Lng)
    ) {
        return false
    }

    fun ccw(ax: Double, ay: Double, bx: Double, by: Double, cx: Double, cy: Double): Double {
        return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax)
    }

    val d1 = ccw(p0Lat, p0Lng, p1Lat, p1Lng, p2Lat, p2Lng)
    val d2 = ccw(p0Lat, p0Lng, p1Lat, p1Lng, p3Lat, p3Lng)
    val d3 = ccw(p2Lat, p2Lng, p3Lat, p3Lng, p0Lat, p0Lng)
    val d4 = ccw(p2Lat, p2Lng, p3Lat, p3Lng, p1Lat, p1Lng)

    return ((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
           ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))
}

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0
    val dLat = (lat2 - lat1) * PI / 180.0
    val dLon = (lon2 - lon1) * PI / 180.0
    val a = sin(dLat / 2).pow(2) + cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
