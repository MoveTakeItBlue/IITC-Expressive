package io.github.movetakeitblue.iitcexpressive.plugin.ui.crosslinks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.movetakeitblue.iitcexpressive.model.NativeLink
import io.github.movetakeitblue.iitcexpressive.plugin.api.*
import io.github.movetakeitblue.iitcexpressive.theme.*
import kotlin.math.*

data class CrossLinkItem(
    val link: NativeLink,
    val drawnShapeId: String,
    val intersectLat: Double,
    val intersectLng: Double,
    val lengthMeters: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossLinksSheet(
    host: IitcPluginHost,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawnShapes by host.drawnShapes.collectAsState()
    val links by host.visibleLinks.collectAsState()

    // Detect all intersecting links
    val crossLinks = remember(drawnShapes, links) {
        detectCrossLinks(drawnShapes, links)
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
                            imageVector = Icons.Default.AltRoute,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cross Links Detection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${drawnShapes.size} drawn shapes • ${links.size} map links analyzed",
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

            // Status Banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (crossLinks.isEmpty()) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (crossLinks.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (crossLinks.isEmpty()) EnlGreen else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (crossLinks.isEmpty()) "No Cross Links Detected" else "${crossLinks.size} Cross Links Detected",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (crossLinks.isEmpty()) EnlGreen else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (crossLinks.isEmpty()) "All planned drawn lines are unobstructed by existing links." else "Existing links intersect with your drawn plans.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (crossLinks.isNotEmpty()) {
                Text(
                    text = "CONFLICTING LINKS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(crossLinks) { item ->
                        val factionColor = when (item.link.team) {
                            1 -> ResBlue
                            2 -> EnlGreen
                            3 -> MacRed
                            else -> NeutralOrange
                        }
                        val factionName = when (item.link.team) {
                            1 -> "RES"
                            2 -> "ENL"
                            3 -> "MAC"
                            else -> "NEU"
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = factionColor.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = factionName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = factionColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Intersecting Link",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${formatDistance(item.lengthMeters)} • Conflict at ${formatCoord(item.intersectLat)}, ${formatCoord(item.intersectLng)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        host.flyTo(item.intersectLat, item.intersectLng, zoom = 16.0)
                                        onDismiss()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NearMe,
                                        contentDescription = "Fly to intersection",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun detectCrossLinks(shapes: List<DrawnShape>, links: List<NativeLink>): List<CrossLinkItem> {
    val result = mutableListOf<CrossLinkItem>()
    val drawnSegments = mutableListOf<Pair<String, Pair<ShapeLatLng, ShapeLatLng>>>()

    shapes.forEach { shape ->
        when (shape) {
            is DrawnShape.Polyline -> {
                for (i in 0 until shape.latLngs.size - 1) {
                    drawnSegments.add(Pair(shape.id, Pair(shape.latLngs[i], shape.latLngs[i + 1])))
                }
            }
            is DrawnShape.Polygon -> {
                for (i in shape.latLngs.indices) {
                    val p1 = shape.latLngs[i]
                    val p2 = shape.latLngs[(i + 1) % shape.latLngs.size]
                    drawnSegments.add(Pair(shape.id, Pair(p1, p2)))
                }
            }
            else -> {}
        }
    }

    if (drawnSegments.isEmpty() || links.isEmpty()) return emptyList()

    links.forEach { link ->
        val linkP1 = ShapeLatLng(link.oLat, link.oLng)
        val linkP2 = ShapeLatLng(link.dLat, link.dLng)
        val dist = calculateDistanceMeters(link.oLat, link.oLng, link.dLat, link.dLng)

        drawnSegments.forEach { (shapeId, drawnSeg) ->
            val intersection = checkLineIntersection(drawnSeg.first, drawnSeg.second, linkP1, linkP2)
            if (intersection != null) {
                result.add(
                    CrossLinkItem(
                        link = link,
                        drawnShapeId = shapeId,
                        intersectLat = intersection.lat,
                        intersectLng = intersection.lng,
                        lengthMeters = dist
                    )
                )
            }
        }
    }

    return result.distinctBy { it.link.guid }
}

private fun checkLineIntersection(
    p1: ShapeLatLng,
    p2: ShapeLatLng,
    p3: ShapeLatLng,
    p4: ShapeLatLng
): ShapeLatLng? {
    // Check if points are identical (shared portal anchor)
    if (isSamePoint(p1, p3) || isSamePoint(p1, p4) || isSamePoint(p2, p3) || isSamePoint(p2, p4)) {
        return null
    }

    fun ccw(a: ShapeLatLng, b: ShapeLatLng, c: ShapeLatLng): Double {
        return (c.lng - a.lng) * (b.lat - a.lat) - (b.lng - a.lng) * (c.lat - a.lat)
    }

    val d1 = ccw(p1, p2, p3)
    val d2 = ccw(p1, p2, p4)
    val d3 = ccw(p3, p4, p1)
    val d4 = ccw(p3, p4, p2)

    if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
        ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))
    ) {
        val midLat = (p1.lat + p2.lat + p3.lat + p4.lat) / 4.0
        val midLng = (p1.lng + p2.lng + p3.lng + p4.lng) / 4.0
        return ShapeLatLng(midLat, midLng)
    }

    return null
}

private fun isSamePoint(a: ShapeLatLng, b: ShapeLatLng): Boolean {
    return abs(a.lat - b.lat) < 0.00005 && abs(a.lng - b.lng) < 0.00005
}

private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0 // Earth radius in meters
    val dLat = (lat2 - lat1) * (PI / 180.0)
    val dLon = (lon2 - lon1) * (PI / 180.0)
    val a = sin(dLat / 2).pow(2) + cos(lat1 * (PI / 180.0)) * cos(lat2 * (PI / 180.0)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

private fun formatDistance(meters: Double): String {
    return if (meters >= 1000) {
        "${(meters / 1000.0 * 10).roundToInt() / 10.0} km"
    } else {
        "${meters.roundToInt()} m"
    }
}

private fun formatCoord(value: Double): String {
    return ((value * 10000.0).roundToInt() / 10000.0).toString()
}
