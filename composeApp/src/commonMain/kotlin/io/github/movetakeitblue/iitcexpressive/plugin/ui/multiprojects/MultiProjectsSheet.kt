package io.github.movetakeitblue.iitcexpressive.plugin.ui.multiprojects

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils
import io.github.movetakeitblue.iitcexpressive.plugin.api.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class OperationProject(
    val id: String,
    val name: String,
    val description: String = "",
    val color: String = "#a24ac3",
    val shapesJson: String = "[]",
    val updatedAtMillis: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiProjectsSheet(
    host: IitcPluginHost,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawnShapes by host.drawnShapes.collectAsState()
    val bookmarkFolders by host.bookmarkFolders.collectAsState()

    var projects by remember {
        mutableStateOf(
            listOf(
                OperationProject(
                    id = "default_project",
                    name = "Primary Operation Plan",
                    description = "Active field and anchor plan",
                    color = "#a24ac3",
                    shapesJson = DrawnShape.toIitcJson(drawnShapes),
                    updatedAtMillis = PlatformUtils.currentTimeMillis()
                )
            )
        )
    }

    var activeProjectId by remember { mutableStateOf("default_project") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var newProjectDesc by remember { mutableStateOf("") }
    var newProjectColor by remember { mutableStateOf("#004785") }

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
                            imageVector = Icons.Default.FolderSpecial,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Multi Projects",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${projects.size} Projects • Isolated Draw Plans & Bookmarks",
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

            // Create New Project Button
            Button(
                onClick = { showCreateDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Project Snapshot", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Projects List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(projects, key = { it.id }) { project ->
                    val isActive = project.id == activeProjectId
                    val shapeCount = remember(project.shapesJson) {
                        DrawnShape.parseIitcJson(project.shapesJson).size
                    }

                    ProjectCard(
                        project = project,
                        isActive = isActive,
                        shapeCount = shapeCount,
                        onActivate = {
                            activeProjectId = project.id
                            val loadedShapes = DrawnShape.parseIitcJson(project.shapesJson)
                            host.saveDrawnShapes(loadedShapes)
                            host.showToast("Switched to project '${project.name}'")
                        },
                        onSaveCurrent = {
                            val currentJson = DrawnShape.toIitcJson(drawnShapes)
                            projects = projects.map {
                                if (it.id == project.id) {
                                    it.copy(
                                        shapesJson = currentJson,
                                        updatedAtMillis = PlatformUtils.currentTimeMillis()
                                    )
                                } else it
                            }
                            host.showToast("Saved active map shapes to '${project.name}'")
                        },
                        onDelete = {
                            if (projects.size > 1) {
                                projects = projects.filterNot { it.id == project.id }
                                if (activeProjectId == project.id) {
                                    activeProjectId = projects.first().id
                                }
                                host.showToast("Project '${project.name}' deleted")
                            } else {
                                host.showToast("Cannot delete the only remaining project")
                            }
                        }
                    )
                }
            }
        }
    }

    // New Project Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Operation Project", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        label = { Text("Project Name") },
                        placeholder = { Text("e.g. Field Day Alpha") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newProjectDesc,
                        onValueChange = { newProjectDesc = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("e.g. 5-layer BAF over downtown") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Current ${drawnShapes.size} shapes will be snapshotted into this project.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newProjectName.ifBlank { "Project ${projects.size + 1}" }
                        val newProj = OperationProject(
                            id = "proj_${PlatformUtils.currentTimeMillis()}",
                            name = name,
                            description = newProjectDesc,
                            color = newProjectColor,
                            shapesJson = DrawnShape.toIitcJson(drawnShapes),
                            updatedAtMillis = PlatformUtils.currentTimeMillis()
                        )
                        projects = projects + newProj
                        activeProjectId = newProj.id
                        showCreateDialog = false
                        newProjectName = ""
                        newProjectDesc = ""
                        host.showToast("Created and switched to '$name'")
                    }
                ) {
                    Text("Create & Switch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProjectCard(
    project: OperationProject,
    isActive: Boolean,
    shapeCount: Int,
    onActivate: () -> Unit,
    onSaveCurrent: () -> Unit,
    onDelete: () -> Unit
) {
    val projectColor = try {
        Color(project.color.removePrefix("#").toLong(16) or 0xFF000000)
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainer
        ),
        border = if (isActive) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(projectColor)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (isActive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (project.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$shapeCount Drawn Shapes stored in project snapshot",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isActive) {
                    Button(
                        onClick = onActivate,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Switch", fontSize = 12.sp)
                    }
                } else {
                    FilledTonalButton(
                        onClick = onSaveCurrent,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Current", fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = onSaveCurrent,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Update", fontSize = 12.sp)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Project",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
