package io.github.movetakeitblue.iitcexpressive.plugin.ui.portallist

import androidx.compose.foundation.background
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
import io.github.movetakeitblue.iitcexpressive.model.NativePortal
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.theme.*

private enum class PortalSortOption(val label: String) {
    LEVEL_DESC("Level (High)"),
    NAME_ASC("Name (A-Z)"),
    HEALTH_ASC("Health (Low)"),
    RESONATORS_DESC("Resonators (High)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalListSheet(
    host: IitcPluginHost,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val portals by host.visiblePortals.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTeamFilter by remember { mutableStateOf<Int?>(-1) } // -1 = All, 0 = Neu, 1 = Res, 2 = Enl, 3 = Mac
    var minLevelFilter by remember { mutableStateOf(0) }
    var currentSort by remember { mutableStateOf(PortalSortOption.LEVEL_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Faction breakdown counts
    val resCount = remember(portals) { portals.count { it.team == 1 } }
    val enlCount = remember(portals) { portals.count { it.team == 2 } }
    val macCount = remember(portals) { portals.count { it.team == 3 } }
    val neuCount = remember(portals) { portals.count { it.team == 0 } }
    val totalCount = portals.size

    val filteredPortals = remember(portals, searchQuery, selectedTeamFilter, minLevelFilter, currentSort) {
        portals
            .filter { p ->
                val matchesTeam = (selectedTeamFilter == -1 || p.team == selectedTeamFilter)
                val matchesLevel = p.level >= minLevelFilter
                val matchesSearch = searchQuery.isBlank() ||
                    p.title.contains(searchQuery, ignoreCase = true) ||
                    p.guid.contains(searchQuery, ignoreCase = true)
                matchesTeam && matchesLevel && matchesSearch
            }
            .let { list ->
                when (currentSort) {
                    PortalSortOption.LEVEL_DESC -> list.sortedWith(compareByDescending<NativePortal> { it.level }.thenBy { it.title })
                    PortalSortOption.NAME_ASC -> list.sortedBy { it.title.lowercase() }
                    PortalSortOption.HEALTH_ASC -> list.sortedWith(compareBy<NativePortal> { it.health }.thenByDescending { it.level })
                    PortalSortOption.RESONATORS_DESC -> list.sortedWith(compareByDescending<NativePortal> { it.resCount }.thenByDescending { it.level })
                }
            }
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
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Portals in View",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$totalCount loaded • showing ${filteredPortals.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sort Button & Dropdown
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort Portals",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        PortalSortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.label,
                                        fontWeight = if (currentSort == option) FontWeight.Bold else FontWeight.Normal,
                                        color = if (currentSort == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    currentSort = option
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Faction Ratio Summary Bar
            if (totalCount > 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        if (resCount > 0) {
                            Box(modifier = Modifier.weight(resCount.toFloat()).fillMaxHeight().background(ResBlue))
                        }
                        if (enlCount > 0) {
                            Box(modifier = Modifier.weight(enlCount.toFloat()).fillMaxHeight().background(EnlGreen))
                        }
                        if (macCount > 0) {
                            Box(modifier = Modifier.weight(macCount.toFloat()).fillMaxHeight().background(MacRed))
                        }
                        if (neuCount > 0) {
                            Box(modifier = Modifier.weight(neuCount.toFloat()).fillMaxHeight().background(NeutralOrange.copy(alpha = 0.5f)))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("RES: $resCount", style = MaterialTheme.typography.labelSmall, color = ResBlue, fontWeight = FontWeight.Bold)
                        Text("ENL: $enlCount", style = MaterialTheme.typography.labelSmall, color = EnlGreen, fontWeight = FontWeight.Bold)
                        Text("MAC: $macCount", style = MaterialTheme.typography.labelSmall, color = MacRed, fontWeight = FontWeight.Bold)
                        Text("NEU: $neuCount", style = MaterialTheme.typography.labelSmall, color = NeutralOrange, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search portals by title...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .height(52.dp)
            )

            // Faction Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTeamFilter == -1,
                    onClick = { selectedTeamFilter = -1 },
                    label = { Text("All Teams") },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = selectedTeamFilter == 1,
                    onClick = { selectedTeamFilter = 1 },
                    label = { Text("RES ($resCount)") },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = selectedTeamFilter == 2,
                    onClick = { selectedTeamFilter = 2 },
                    label = { Text("ENL ($enlCount)") },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = selectedTeamFilter == 3,
                    onClick = { selectedTeamFilter = 3 },
                    label = { Text("MAC ($macCount)") },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = selectedTeamFilter == 0,
                    onClick = { selectedTeamFilter = 0 },
                    label = { Text("NEU ($neuCount)") },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Portal Card List / Empty State
            if (filteredPortals.isEmpty()) {
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
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (totalCount == 0) "No portals in current view" else "No matching portals",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (totalCount == 0) "Pan or zoom into an area to load portals." else "Try adjusting your filters.",
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
                        .heightIn(max = 500.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPortals, key = { it.guid }) { portal ->
                        PortalListItemCard(
                            portal = portal,
                            isBookmarked = host.isPortalBookmarked(portal.guid),
                            onBookmarkToggle = {
                                val added = host.togglePortalBookmark(portal)
                                host.showToast(if (added) "Bookmarked ${portal.title}" else "Removed bookmark")
                            },
                            onClick = {
                                host.flyTo(portal.lat, portal.lng, 17.0)
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
private fun PortalListItemCard(
    portal: NativePortal,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val factionColor = when (portal.team) {
        1 -> ResBlue
        2 -> EnlGreen
        3 -> MacRed
        else -> NeutralOrange
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
            // Level badge pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = factionColor.copy(alpha = 0.2f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "L${portal.level}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = factionColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = portal.title.ifBlank { "Unknown Portal" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Resonators indicator dots
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                        for (i in 0 until 8) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i < portal.resCount) factionColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }

                    // Health percentage
                    Text(
                        text = "${portal.health}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (portal.health >= 80) factionColor else if (portal.health >= 30) AmberStar else MacRed,
                        fontWeight = FontWeight.Bold
                    )

                    // Coordinates
                    Text(
                        text = "• ${"%.4f".format(portal.lat)}, ${"%.4f".format(portal.lng)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }

            // Quick Star Bookmark Toggle
            IconButton(
                onClick = onBookmarkToggle,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Bookmark Portal",
                    tint = if (isBookmarked) AmberStar else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Quick Fly Button
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = "Fly to portal",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
