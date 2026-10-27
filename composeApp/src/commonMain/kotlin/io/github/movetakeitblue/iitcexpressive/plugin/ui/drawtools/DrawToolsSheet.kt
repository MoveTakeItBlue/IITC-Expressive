package io.github.movetakeitblue.iitcexpressive.plugin.ui.drawtools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils
import io.github.movetakeitblue.iitcexpressive.plugin.api.DrawnShape
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost

val DRAW_PALETTE = listOf(
    "#a24ac3", // Standard Purple
    "#da3535", // Red
    "#f7e50c", // Yellow
    "#2ecc71", // Green
    "#3498db", // Blue
    "#e67e22", // Orange
    "#00e5ff", // Cyan
    "#ffffff"  // White
)

fun parseHexColor(hex: String, fallback: Color = Color(0xFFA24AC3)): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16).toInt()
        if (clean.length == 6) {
            Color(colorInt or 0xFF000000.toInt())
        } else if (clean.length == 8) {
            Color(colorInt)
        } else fallback
    } catch (e: Exception) {
        fallback
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawToolsSheet(
    host: IitcPluginHost,
    onStartDrawing: (type: String, color: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shapes by host.drawnShapes.collectAsState()
    var selectedColor by remember { mutableStateOf("#a24ac3") }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showImportExportDialog by remember { mutableStateOf(false) }
    var importExportText by remember { mutableStateOf("") }

    val lineCount = remember(shapes) { shapes.count { it is DrawnShape.Polyline } }
    val polyCount = remember(shapes) { shapes.count { it is DrawnShape.Polygon } }
    val markerCount = remember(shapes) { shapes.count { it is DrawnShape.Marker || it is DrawnShape.Circle } }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Drawn Items?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove all ${shapes.size} drawn items from the map?") },
            confirmButton = {
                Button(
                    onClick = {
                        host.saveDrawnShapes(emptyList())
                        showClearConfirm = false
                        host.showToast("All drawn items cleared")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showImportExportDialog) {
        AlertDialog(
            onDismissRequest = { showImportExportDialog = false },
            title = { Text("Draw Tools JSON Data", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "IITC-compatible draw-tools layer format:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importExportText,
                        onValueChange = { importExportText = it },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = DrawnShape.parseIitcJson(importExportText)
                        if (parsed.isNotEmpty()) {
                            host.saveDrawnShapes(shapes + parsed)
                            host.showToast("Imported ${parsed.size} drawn shapes")
                        } else {
                            host.showToast("Invalid draw tools JSON")
                        }
                        showImportExportDialog = false
                    }
                ) {
                    Text("Import & Merge")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            val jsonStr = DrawnShape.toIitcJson(shapes)
                            PlatformUtils.copyToClipboard(jsonStr, "Copied Draw Tools JSON")
                            host.showToast("Copied to clipboard")
                        }
                    ) {
                        Text("Copy JSON")
                    }
                    TextButton(onClick = { showImportExportDialog = false }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Gesture,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Draw Tools",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${shapes.size} items ($lineCount lines, $polyCount polygons, $markerCount markers)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Import/Export Button
                IconButton(
                    onClick = {
                        importExportText = DrawnShape.toIitcJson(shapes)
                        showImportExportDialog = true
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Import/Export JSON",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Clear All Button
                if (shapes.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Quick Color Palette
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Color:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DRAW_PALETTE.forEach { hex ->
                        val color = parseHexColor(hex)
                        val isSelected = selectedColor.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)))
                            }
                        }
                    }
                }
            }

            // Start Drawing Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onStartDrawing("polyline", selectedColor)
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(23.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Draw Line", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                FilledTonalButton(
                    onClick = {
                        onDismiss()
                        onStartDrawing("polygon", selectedColor)
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(23.dp)
                ) {
                    Icon(imageVector = Icons.Default.Hexagon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Polygon", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                FilledTonalButton(
                    onClick = {
                        onDismiss()
                        onStartDrawing("marker", selectedColor)
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(23.dp)
                ) {
                    Icon(imageVector = Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Marker", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Drawn Objects List / Empty State
            if (shapes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Draw,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No drawn objects on map",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Draw Line', 'Polygon', or 'Marker' above to plan field operations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 440.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(shapes, key = { it.id }) { shape ->
                        DrawnShapeCard(
                            shape = shape,
                            onClick = {
                                when (shape) {
                                    is DrawnShape.Polyline -> if (shape.latLngs.isNotEmpty()) host.flyTo(shape.latLngs[0].lat, shape.latLngs[0].lng, 16.0)
                                    is DrawnShape.Polygon -> if (shape.latLngs.isNotEmpty()) host.flyTo(shape.latLngs[0].lat, shape.latLngs[0].lng, 16.0)
                                    is DrawnShape.Circle -> host.flyTo(shape.center.lat, shape.center.lng, 16.0)
                                    is DrawnShape.Marker -> host.flyTo(shape.latLng.lat, shape.latLng.lng, 16.0)
                                }
                                onDismiss()
                            },
                            onDelete = {
                                host.saveDrawnShapes(shapes.filter { it.id != shape.id })
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawnShapeCard(
    shape: DrawnShape,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = parseHexColor(shape.color)
    val (typeTitle, typeDesc, icon) = when (shape) {
        is DrawnShape.Polyline -> Triple("Polyline", "${shape.latLngs.size} points", Icons.Default.Timeline)
        is DrawnShape.Polygon -> Triple("Polygon", "${shape.latLngs.size} vertices", Icons.Default.Hexagon)
        is DrawnShape.Circle -> Triple("Circle", "Radius: ${shape.radiusMeters.toInt()}m", Icons.Default.RadioButtonUnchecked)
        is DrawnShape.Marker -> Triple("Marker", if (shape.label.isNotBlank()) shape.label else "${"%.4f".format(shape.latLng.lat)}, ${"%.4f".format(shape.latLng.lng)}", Icons.Default.Place)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color & Shape type indicator badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.25f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = typeDesc,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }

            // Quick Fly Button
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = "Fly to shape",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Delete Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete shape",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
