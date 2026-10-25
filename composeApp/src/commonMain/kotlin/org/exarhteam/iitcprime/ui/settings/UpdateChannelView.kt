package org.exarhteam.iitcprime.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.model.Channel
import org.exarhteam.iitcprime.theme.AccentTeal
import org.exarhteam.iitcprime.theme.SurfaceContainerDark
import org.exarhteam.iitcprime.ui.common.SelectFieldDialog
import org.exarhteam.iitcprime.viewmodel.UpdateChannelViewModel

@Composable
fun UpdateChannelView(
    viewModel: UpdateChannelViewModel,
    onBack: () -> Unit
) {
    val currentChannel by viewModel.currentChannel.collectAsState()
    val customUrl by viewModel.customChannelUrl.collectAsState()
    val updateInterval by viewModel.selectedInterval.collectAsState()
    val externalInterval by viewModel.selectedExternalInterval.collectAsState()
    val isChecking by viewModel.isCheckingUpdates.collectAsState()

    var showCoreIntervalDialog by remember { mutableStateOf(false) }
    var showExternalIntervalDialog by remember { mutableStateOf(false) }

    val coreIntervals = listOf(
        Pair(0L, L("update_channel.interval.disabled")),
        Pair(21600L, L("update_channel.interval.6_hours")),
        Pair(43200L, L("update_channel.interval.12_hours")),
        Pair(86400L, L("update_channel.interval.1_day")),
        Pair(604800L, L("update_channel.interval.1_week"))
    )

    val externalIntervals = listOf(
        Pair(0L, L("update_channel.interval.disabled")),
        Pair(86400L, L("update_channel.interval.1_day")),
        Pair(604800L, L("update_channel.interval.1_week")),
        Pair(2592000L, L("update_channel.interval.1_month"))
    )

    if (showCoreIntervalDialog) {
        val currentIdx = coreIntervals.indexOfFirst { it.first == updateInterval }.coerceAtLeast(0)
        SelectFieldDialog(
            title = L("update_channel.section.frequency"),
            items = coreIntervals.map { it.second },
            selectedIndex = currentIdx,
            onSelect = { idx ->
                if (idx in coreIntervals.indices) {
                    viewModel.setUpdateInterval(coreIntervals[idx].first)
                }
            },
            onDismiss = { showCoreIntervalDialog = false }
        )
    }

    if (showExternalIntervalDialog) {
        val currentIdx = externalIntervals.indexOfFirst { it.first == externalInterval }.coerceAtLeast(0)
        SelectFieldDialog(
            title = L("update_channel.section.external_frequency"),
            items = externalIntervals.map { it.second },
            selectedIndex = currentIdx,
            onSelect = { idx ->
                if (idx in externalIntervals.indices) {
                    viewModel.setExternalUpdateInterval(externalIntervals[idx].first)
                }
            },
            onDismiss = { showExternalIntervalDialog = false }
        )
    }

    SettingsBase(
        title = L("update_channel.title"),
        onBack = onBack,
        headerRight = {
            TextButton(
                onClick = { viewModel.checkUpdatesNow() },
                enabled = !isChecking
            ) {
                Text(
                    text = L("update_channel.update_now"),
                    color = if (!isChecking) AccentTeal else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) {
        // Channel selection
        SettingsSection(title = L("update_channel.section.channel"))

        val channels = listOf(
            Triple(Channel.RELEASE, L("update_channel.channel.release"), L("update_channel.channel.release.description")),
            Triple(Channel.BETA, L("update_channel.channel.beta"), L("update_channel.channel.beta.description")),
            Triple(Channel.CUSTOM, L("update_channel.channel.custom"), L("update_channel.channel.custom.description"))
        )

        channels.forEachIndexed { index, (type, name, desc) ->
            val isFirst = index == 0
            val isLast = index == channels.size - 1
            val cornerShape = when {
                isFirst && isLast -> RoundedCornerShape(12.dp)
                isFirst -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                isLast -> RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                else -> RoundedCornerShape(0.dp)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cornerShape)
                    .background(SurfaceContainerDark)
                    .clickable { viewModel.selectChannel(type) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }

                    RadioButton(
                        selected = currentChannel == type,
                        onClick = { viewModel.selectChannel(type) }
                    )
                }

                if (!isLast) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        // Custom URL input
        if (currentChannel == Channel.CUSTOM) {
            SettingsSection(title = L("update_channel.section.custom_url"))
            OutlinedTextField(
                value = customUrl,
                onValueChange = { viewModel.setCustomUrl(it) },
                placeholder = { Text("https://example.com/iitc/") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerDark),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentTeal,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        // Core Update frequency
        SettingsSection(title = L("update_channel.section.frequency"))
        val coreLabel = coreIntervals.find { it.first == updateInterval }?.second ?: ""
        NavSettingsItem(
            title = L("update_channel.section.frequency"),
            description = coreLabel,
            isFirst = true,
            isLast = true,
            onClick = { showCoreIntervalDialog = true }
        )

        // External plugins update frequency
        SettingsSection(title = L("update_channel.section.external_frequency"))
        val extLabel = externalIntervals.find { it.first == externalInterval }?.second ?: ""
        NavSettingsItem(
            title = L("update_channel.section.external_frequency"),
            description = extLabel,
            isFirst = true,
            isLast = true,
            onClick = { showExternalIntervalDialog = true }
        )

        // Status & Last check
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isChecking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AccentTeal,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = L("update_channel.checking"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            } else {
                Text(
                    text = viewModel.getFormattedLastCheck(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }
    }
}
