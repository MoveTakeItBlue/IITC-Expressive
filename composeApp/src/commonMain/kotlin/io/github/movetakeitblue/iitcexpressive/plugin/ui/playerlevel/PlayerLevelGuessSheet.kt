package io.github.movetakeitblue.iitcexpressive.plugin.ui.playerlevel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import io.github.movetakeitblue.iitcexpressive.model.ChatMessage
import io.github.movetakeitblue.iitcexpressive.model.NativePortal
import io.github.movetakeitblue.iitcexpressive.model.PortalStatus
import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.theme.*

data class InferredPlayer(
    val codename: String,
    val team: String,
    val minLevel: Int,
    val evidence: String,
    val lastSeenTime: Long,
    val lastSeenPortal: String,
    val portalLat: Double = 0.0,
    val portalLng: Double = 0.0,
    val actionCount: Int = 1
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerLevelGuessSheet(
    host: IitcPluginHost,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chatMessagesMap by host.chatMessages.collectAsState()
    val portals by host.visiblePortals.collectAsState()
    val selectedPortal by host.selectedPortal.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var factionFilter by remember { mutableStateOf("ALL") } // ALL, RESISTANCE, ENLIGHTENED, MACHINA
    var sortOrder by remember { mutableStateOf(0) } // 0: Level desc, 1: Time desc, 2: Name asc

    // Analyze players
    val inferredPlayers = remember(chatMessagesMap, portals, selectedPortal) {
        deducePlayerLevels(chatMessagesMap, portals, selectedPortal)
    }

    val filteredPlayers = remember(inferredPlayers, searchQuery, factionFilter, sortOrder) {
        inferredPlayers.filter { p ->
            (factionFilter == "ALL" || p.team.equals(factionFilter, ignoreCase = true)) &&
            (searchQuery.isBlank() || p.codename.contains(searchQuery.trim(), ignoreCase = true))
        }.let { list ->
            when (sortOrder) {
                0 -> list.sortedWith(compareByDescending<InferredPlayer> { it.minLevel }.thenByDescending { it.lastSeenTime })
                1 -> list.sortedByDescending { it.lastSeenTime }
                2 -> list.sortedBy { it.codename.lowercase() }
                else -> list
            }
        }
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
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Player Level Guess",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${inferredPlayers.size} Agents Detected • COMM & Portal Analysis",
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

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search agent codename...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            // Faction Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            ) {
                listOf("ALL" to "All", "RESISTANCE" to "RES", "ENLIGHTENED" to "ENL", "MACHINA" to "MAC").forEach { (fKey, label) ->
                    item {
                        FilterChip(
                            selected = factionFilter == fKey,
                            onClick = { factionFilter = fKey },
                            label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }

            // Sort Options
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SORT BY:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                listOf("Level" to 0, "Recent" to 1, "Name" to 2).forEach { (sLabel, sIndex) ->
                    FilterChip(
                        selected = sortOrder == sIndex,
                        onClick = { sortOrder = sIndex },
                        label = { Text(sLabel, fontSize = 11.sp) }
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Players List
            if (filteredPlayers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (inferredPlayers.isEmpty()) "No player activity recorded yet" else "No matching agents found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "COMM messages or portal resonators will populate agent levels",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    items(filteredPlayers, key = { it.codename }) { player ->
                        PlayerGuessCard(
                            player = player,
                            onFlyToPortal = {
                                if (player.portalLat != 0.0 && player.portalLng != 0.0) {
                                    host.flyTo(player.portalLat, player.portalLng, zoom = 17.0)
                                    onDismiss()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerGuessCard(
    player: InferredPlayer,
    onFlyToPortal: () -> Unit
) {
    val factionColor = when (player.team.uppercase()) {
        "RESISTANCE", "RES" -> TeamResistance
        "ENLIGHTENED", "ENL" -> TeamEnlightened
        "MACHINA", "MAC" -> TeamMachina
        else -> TeamNeutral
    }

    val factionContainer = when (player.team.uppercase()) {
        "RESISTANCE", "RES" -> TeamResistanceContainer
        "ENLIGHTENED", "ENL" -> TeamEnlightenedContainer
        "MACHINA", "MAC" -> TeamMachinaContainer
        else -> TeamNeutralContainer
    }

    val levelColor = when (player.minLevel) {
        1 -> Level1Color
        2 -> Level2Color
        3 -> Level3Color
        4 -> Level4Color
        5 -> Level5Color
        6 -> Level6Color
        7 -> Level7Color
        8 -> Level8Color
        in 9..16 -> Color(0xFF9627F4) // L9-L16 Exotic Purple
        else -> Level0Color
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = levelColor.copy(alpha = 0.2f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "L${player.minLevel}+",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = levelColor
                        )
                        Text(
                            text = "LEVEL",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = levelColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Agent Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.codename,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = factionColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = factionContainer
                    ) {
                        Text(
                            text = when (player.team.uppercase()) {
                                "RESISTANCE", "RES" -> "RES"
                                "ENLIGHTENED", "ENL" -> "ENL"
                                "MACHINA", "MAC" -> "MAC"
                                else -> "NEU"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = factionColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Evidence row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = player.evidence,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (player.lastSeenPortal.isNotBlank()) {
                    Text(
                        text = "At: ${player.lastSeenPortal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick Jump to portal
            if (player.portalLat != 0.0 && player.portalLng != 0.0) {
                IconButton(
                    onClick = onFlyToPortal,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = "Fly to portal",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Deduces player levels from COMM messages and portal resonators.
 */
private fun deducePlayerLevels(
    chatMessagesMap: Map<String, List<ChatMessage>>,
    portals: List<NativePortal>,
    selectedPortal: PortalStatus
): List<InferredPlayer> {
    val playerMap = mutableMapOf<String, InferredPlayer>()

    // 1. Analyze selected portal resonators (direct owner data)
    for (reso in selectedPortal.resonators.orEmpty()) {
        val owner = reso.owner?.trim() ?: continue
        if (owner.isBlank()) continue
        val current = playerMap[owner]
        val currentLvl = current?.minLevel ?: 1
        val newLvl = maxOf(currentLvl, reso.level)
        val evidence = "Deployed L${reso.level} Resonator"
        playerMap[owner] = InferredPlayer(
            codename = owner,
            team = selectedPortal.team,
            minLevel = newLvl,
            evidence = evidence,
            lastSeenTime = PlatformUtils.currentTimeMillis(),
            lastSeenPortal = selectedPortal.title,
            portalLat = selectedPortal.latE6?.div(1e6) ?: 0.0,
            portalLng = selectedPortal.lngE6?.div(1e6) ?: 0.0,
            actionCount = (current?.actionCount ?: 0) + 1
        )
    }

    // 2. Analyze COMM chat messages
    val allMessages = chatMessagesMap.values.flatten()
    for (msg in allMessages) {
        val sender = msg.sender.trim()
        if (sender.isBlank()) continue

        val text = msg.text
        var guessedLevel = 1
        var evidence = "COMM Activity"

        // Pattern matching for resonator deployments
        val lRegex = Regex("L([1-8])\\s+Resonator", RegexOption.IGNORE_CASE)
        val match = lRegex.find(text)
        if (match != null) {
            val lvl = match.groupValues[1].toIntOrNull() ?: 1
            guessedLevel = lvl
            evidence = "Deployed L$lvl Resonator"
        } else if (text.contains("linked", ignoreCase = true)) {
            guessedLevel = maxOf(guessedLevel, 2)
            evidence = "Created Link"
        } else if (text.contains("Control Field", ignoreCase = true)) {
            guessedLevel = maxOf(guessedLevel, 2)
            evidence = "Created Field"
        } else if (text.contains("captured", ignoreCase = true)) {
            guessedLevel = maxOf(guessedLevel, 1)
            evidence = "Captured Portal"
        } else if (text.contains("destroyed", ignoreCase = true)) {
            guessedLevel = maxOf(guessedLevel, 1)
            evidence = "Destroyed Resonator"
        }

        val existing = playerMap[sender]
        val bestLevel = maxOf(existing?.minLevel ?: 1, guessedLevel)
        val bestEvidence = if (guessedLevel >= (existing?.minLevel ?: 1)) evidence else (existing?.evidence ?: evidence)
        val portalName = msg.portal?.name?.ifBlank { existing?.lastSeenPortal ?: "" } ?: (existing?.lastSeenPortal ?: "")
        val lat = msg.portal?.lat?.takeIf { it != 0.0 } ?: (existing?.portalLat ?: 0.0)
        val lng = msg.portal?.lng?.takeIf { it != 0.0 } ?: (existing?.portalLng ?: 0.0)

        playerMap[sender] = InferredPlayer(
            codename = sender,
            team = if (msg.team != "NEUTRAL") msg.team else (existing?.team ?: "NEUTRAL"),
            minLevel = bestLevel,
            evidence = bestEvidence,
            lastSeenTime = maxOf(existing?.lastSeenTime ?: 0L, msg.time),
            lastSeenPortal = portalName,
            portalLat = lat,
            portalLng = lng,
            actionCount = (existing?.actionCount ?: 0) + 1
        )
    }

    return playerMap.values.toList()
}
