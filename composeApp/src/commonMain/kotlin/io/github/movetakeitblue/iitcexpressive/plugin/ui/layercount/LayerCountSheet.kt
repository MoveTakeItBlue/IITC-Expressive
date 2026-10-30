package io.github.movetakeitblue.iitcexpressive.plugin.ui.layercount

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.movetakeitblue.iitcexpressive.model.NativeField
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayerCountSheet(
    host: IitcPluginHost,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entities by host.visibleEntities.collectAsState()
    val fields = entities.fields

    val resFields = remember(fields) { fields.filter { it.team == 1 } }
    val enlFields = remember(fields) { fields.filter { it.team == 2 } }
    val macFields = remember(fields) { fields.filter { it.team == 3 } }

    // Calculate nested depth estimation
    val maxLayers = remember(fields) {
        calculateMaxLayers(fields)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Layer Count",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Control fields and nested depth",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Max Layer Highlight Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "MAX NESTED LAYERS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (fields.isEmpty()) "0 Layers" else "$maxLayers Layers",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Estimated deepest overlapping field stack",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$maxLayers",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Faction Fields Breakdown
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "FIELDS BY FACTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )

                    // Visual Progress Bar
                    val total = (resFields.size + enlFields.size + macFields.size).coerceAtLeast(1).toFloat()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        if (resFields.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(resFields.size / total)
                                    .fillMaxHeight()
                                    .background(ResBlue)
                            )
                        }
                        if (enlFields.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(enlFields.size / total)
                                    .fillMaxHeight()
                                    .background(EnlGreen)
                            )
                        }
                        if (macFields.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(macFields.size / total)
                                    .fillMaxHeight()
                                    .background(MacRed)
                            )
                        }
                        if (fields.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                        }
                    }

                    // Count Rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FactionFieldStat(name = "Resistance", count = resFields.size, color = ResBlue)
                        FactionFieldStat(name = "Enlightened", count = enlFields.size, color = EnlGreen)
                        FactionFieldStat(name = "Machina", count = macFields.size, color = MacRed)
                        FactionFieldStat(name = "Total", count = fields.size, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notice
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Layer count analysis calculates multi-layer nesting by testing field polygon intersections and containment within the visible viewport.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FactionFieldStat(name: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Calculates the maximum nested layer count among a set of fields
 * using polygon point-in-triangle containment.
 */
private fun calculateMaxLayers(fields: List<NativeField>): Int {
    if (fields.isEmpty()) return 0
    if (fields.size == 1) return 1

    var maxDepth = 1
    // Sample centroids of each field
    for (f in fields) {
        val pts = f.points
        if (pts.size < 3) continue
        val cLat = (pts[0][0] + pts[1][0] + pts[2][0]) / 3.0
        val cLng = (pts[0][1] + pts[1][1] + pts[2][1]) / 3.0

        var depth = 0
        for (other in fields) {
            val op = other.points
            if (op.size >= 3 && isPointInTriangle(cLat, cLng, op[0][0], op[0][1], op[1][0], op[1][1], op[2][0], op[2][1])) {
                depth++
            }
        }
        if (depth > maxDepth) maxDepth = depth
    }
    return maxDepth
}

private fun isPointInTriangle(
    px: Double, py: Double,
    ax: Double, ay: Double,
    bx: Double, by: Double,
    cx: Double, cy: Double
): Boolean {
    val d1 = sign(px, py, ax, ay, bx, by)
    val d2 = sign(px, py, bx, by, cx, cy)
    val d3 = sign(px, py, cx, cy, ax, ay)
    val hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0)
    val hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0)
    return !(hasNeg && hasPos)
}

private fun sign(p1x: Double, p1y: Double, p2x: Double, p2y: Double, p3x: Double, p3y: Double): Double {
    return (p1x - p3x) * (p2y - p3y) - (p2x - p3x) * (p1y - p3y)
}
