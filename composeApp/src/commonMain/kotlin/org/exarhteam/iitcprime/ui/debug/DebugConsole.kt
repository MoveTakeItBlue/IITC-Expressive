package org.exarhteam.iitcprime.ui.debug

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.exarhteam.iitcprime.model.LogItem
import org.exarhteam.iitcprime.platform.PlatformUtils
import org.exarhteam.iitcprime.viewmodel.MainViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DebugConsole(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.logs.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var command by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(listOf<String>()) }
    var historyIndex by remember { mutableStateOf(-1) }

    // Auto-scroll to bottom when new logs arrive if already near bottom
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) true
            else {
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                lastVisibleItem != null && lastVisibleItem.index >= totalItems - 1
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Logs display list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logs, key = { it.id }) { item ->
                    val (headerColor: Color, messageColor: Color) = when (item.type.lowercase()) {
                        "error" -> Pair(Color(0xFFE57373), Color(0xFFFFCDD2))
                        "warn", "warning" -> Pair(Color(0xFFFFB74D), Color(0xFFFFE0B2))
                        "debug" -> Pair(Color(0xFF81D4FA), Color(0xFFE1F5FE))
                        else -> Pair(Color(0xFF80CBC4), MaterialTheme.colorScheme.onSurface)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f))
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    PlatformUtils.copyToClipboard(item.message)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "[${item.time}] [${item.type.uppercase()}] ${item.source}",
                            color = headerColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.message,
                            color = messageColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Controls panel at bottom
            ControlsPanel(
                command = command,
                onCommandChange = { command = it },
                onExecuteCommand = { cmd ->
                    viewModel.executeDebugCommand(cmd)
                    if (cmd.isNotBlank() && (history.isEmpty() || history.last() != cmd)) {
                        history = history + cmd
                    }
                    historyIndex = -1
                },
                onClearLogs = { viewModel.clearLogs() },
                onHistoryUp = {
                    if (history.isNotEmpty()) {
                        val newIdx = if (historyIndex == -1) history.size - 1 else (historyIndex - 1).coerceAtLeast(0)
                        historyIndex = newIdx
                        command = history[newIdx]
                    }
                },
                onHistoryDown = {
                    if (history.isNotEmpty() && historyIndex != -1) {
                        val newIdx = historyIndex + 1
                        if (newIdx < history.size) {
                            historyIndex = newIdx
                            command = history[newIdx]
                        } else {
                            historyIndex = -1
                            command = ""
                        }
                    }
                },
                onClose = { viewModel.toggleDebugMode() }
            )
        }

        // Scroll to bottom FAB
        AnimatedVisibility(
            visible = !isAtBottom,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 120.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    if (logs.isNotEmpty()) {
                        scope.launch {
                            listState.animateScrollToItem(logs.size - 1)
                        }
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Scroll to bottom"
                )
            }
        }
    }
}
