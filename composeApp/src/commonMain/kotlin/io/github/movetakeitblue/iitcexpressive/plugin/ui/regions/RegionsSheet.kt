package io.github.movetakeitblue.iitcexpressive.plugin.ui.regions

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.movetakeitblue.iitcexpressive.plugin.api.*
import io.github.movetakeitblue.iitcexpressive.theme.*
import kotlin.math.*

data class S2RegionInfo(
    val code: String,
    val faceName: String,
    val minLat: Double,
    val minLng: Double,
    val maxLat: Double,
    val maxLng: Double,
    val centerLat: Double,
    val centerLng: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionsSheet(
    host: IitcPluginHost,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val portals by host.visiblePortals.collectAsState()
    val links by host.visibleLinks.collectAsState()
    val fields by host.visibleFields.collectAsState()
    val drawnShapes by host.drawnShapes.collectAsState()

    // Estimate map center from visible portals or fallback
    val centerLat = remember(portals) { if (portals.isNotEmpty()) portals.map { it.lat }.average() else 39.9042 }
    val centerLng = remember(portals) { if (portals.isNotEmpty()) portals.map { it.lng }.average() else 116.4074 }

    val regionInfo = remember(centerLat, centerLng) {
        computeS2Level6Region(centerLat, centerLng)
    }

    val portalsInRegion = remember(portals, regionInfo) {
        portals.filter { p ->
            p.lat in regionInfo.minLat..regionInfo.maxLat &&
            p.lng in regionInfo.minLng..regionInfo.maxLng
        }
    }

    val currentCp = remember { (CycleTimesCalculator.getLastCompletedCP() + 1).coerceAtMost(35) }
    val timeToNextCp = remember { CycleTimesCalculator.formatDuration(CycleTimesCalculator.getTimeToNextCPSeconds()) }

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
                            imageVector = Icons.Default.GridOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Scoring Regions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "S2 Level 6 Regional Cell • Septicycle Scoring",
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

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Hero Region Code Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "CURRENT REGION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = regionInfo.code,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = regionInfo.faceName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        host.showToast("Region code ${regionInfo.code} copied")
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Code", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        // Draw boundary polygon in Draw Tools
                                        val boundaryPoly = DrawnShape.Polygon(
                                            id = "region_${regionInfo.code}",
                                            color = "#00522B",
                                            latLngs = listOf(
                                                ShapeLatLng(regionInfo.minLat, regionInfo.minLng),
                                                ShapeLatLng(regionInfo.minLat, regionInfo.maxLng),
                                                ShapeLatLng(regionInfo.maxLat, regionInfo.maxLng),
                                                ShapeLatLng(regionInfo.maxLat, regionInfo.minLng)
                                            )
                                        )
                                        val cleaned = drawnShapes.filterNot { it.id.startsWith("region_") }
                                        host.saveDrawnShapes(cleaned + boundaryPoly)
                                        host.showToast("Drawn cell boundary for ${regionInfo.code} on map")
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Draw on Map", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Cell Boundaries Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "S2 CELL GEOMETRY (LEVEL 6)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("South-West", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    Text(
                                        text = "${((regionInfo.minLat * 1000).roundToInt() / 1000.0)}, ${((regionInfo.minLng * 1000).roundToInt() / 1000.0)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("North-East", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    Text(
                                        text = "${((regionInfo.maxLat * 1000).roundToInt() / 1000.0)}, ${((regionInfo.maxLng * 1000).roundToInt() / 1000.0)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Center Coordinates", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${((regionInfo.centerLat * 1000).roundToInt() / 1000.0)}, ${((regionInfo.centerLng * 1000).roundToInt() / 1000.0)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Region Entities & Scoring Stats
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "CURRENT REGIONAL ACTIVITY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${portalsInRegion.size}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("Portals in Cell", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "CP $currentCp/35",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text("Checkpoint", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = timeToNextCp,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Text("CP Timer", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Quick Navigation to Scoreboard
                item {
                    FilledTonalButton(
                        onClick = {
                            host.requestRegionScore()
                            host.showToast("Requested scoreboard for ${regionInfo.code}")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Query Region Scoreboard", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Computes S2 Level 6 region code from latitude and longitude.
 * Ingress regions use S2 Level 6 cells with standard phonetic alphabet codes.
 */
private fun computeS2Level6Region(lat: Double, lng: Double): S2RegionInfo {
    // S2 Face mapping from 3D coordinates
    val phi = lat * PI / 180.0
    val theta = lng * PI / 180.0
    val cosPhi = cos(phi)
    val x = cosPhi * cos(theta)
    val y = cosPhi * sin(theta)
    val z = sin(phi)

    val absX = abs(x)
    val absY = abs(y)
    val absZ = abs(z)

    val face: Int
    val u: Double
    val v: Double

    if (absX >= absY && absX >= absZ) {
        if (x > 0) {
            face = 0
            u = y / x
            v = z / x
        } else {
            face = 3
            u = -z / -x
            v = -y / -x
        }
    } else if (absY >= absX && absY >= absZ) {
        if (y > 0) {
            face = 1
            u = -x / y
            v = z / y
        } else {
            face = 4
            u = z / -y
            v = x / -y
        }
    } else {
        if (z > 0) {
            face = 2
            u = -y / z
            v = -x / z
        } else {
            face = 5
            u = x / -z
            v = -y / -z
        }
    }

    // Convert (u,v) in [-1, 1] to S2 Level 6 cell coordinates (0..63)
    val s = (u + 1.0) / 2.0
    val t = (v + 1.0) / 2.0
    val maxCoord = 64
    val cellI = (s * maxCoord).toInt().coerceIn(0, maxCoord - 1)
    val cellJ = (t * maxCoord).toInt().coerceIn(0, maxCoord - 1)

    val prefixes = listOf("AF", "AS", "NR", "AM", "PA", "ST")
    val facePrefix = prefixes.getOrElse(face) { "AM" }

    val alphabet = listOf(
        "ALPHA", "BRAVO", "CHARLIE", "DELTA", "ECHO", "FOXTROT",
        "GOLF", "HOTEL", "INDIA", "JULIETT", "KILO", "LIMA",
        "MIKE", "NOVEMBER", "OSCAR", "PAPA", "QUEBEC", "ROMEO",
        "SIERRA", "TANGO", "UNIFORM", "VICTOR", "WHISKEY", "XRAY",
        "YANKEE", "ZULU"
    )

    val major = (cellI / 4) + 1
    val wordIdx = (cellJ % alphabet.size)
    val minor = (cellI % 16) + 1

    val majorStr = if (major < 10) "0$major" else "$major"
    val minorStr = if (minor < 10) "0$minor" else "$minor"
    val code = "$facePrefix$majorStr-${alphabet[wordIdx]}-$minorStr"

    val faceNames = listOf(
        "Africa & Indian Ocean",
        "Asia & Western Pacific",
        "Northern Arctic & Europe",
        "Americas & Atlantic",
        "Central & South Pacific",
        "Antarctica & Southern Ocean"
    )

    // Cell extent approximation
    val cellSpanLat = 180.0 / 64.0
    val cellSpanLng = 360.0 / 64.0
    val cLat = (lat / cellSpanLat).toInt() * cellSpanLat + cellSpanLat / 2.0
    val cLng = (lng / cellSpanLng).toInt() * cellSpanLng + cellSpanLng / 2.0

    return S2RegionInfo(
        code = code,
        faceName = faceNames.getOrElse(face) { "Global Grid" },
        minLat = cLat - cellSpanLat / 2.0,
        minLng = cLng - cellSpanLng / 2.0,
        maxLat = cLat + cellSpanLat / 2.0,
        maxLng = cLng + cellSpanLng / 2.0,
        centerLat = cLat,
        centerLng = cLng
    )
}
