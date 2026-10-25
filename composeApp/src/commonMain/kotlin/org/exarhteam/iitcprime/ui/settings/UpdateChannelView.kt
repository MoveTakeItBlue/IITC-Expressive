package org.exarhteam.iitcprime.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ripple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.model.Channel
import org.exarhteam.iitcprime.ui.common.SelectFieldDialog
import org.exarhteam.iitcprime.viewmodel.UpdateChannelViewModel

/**
 * Authentic Material 3 Expressive Update Channel Screen
 */
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
    val colorScheme = MaterialTheme.colorScheme

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
            FilledTonalButton(
                onClick = { viewModel.checkUpdatesNow() },
                enabled = !isChecking,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colorScheme.primaryContainer,
                    contentColor = colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(percent = 50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = L("update_channel.update_now"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) {
        // Channel selection
        SettingsSection(title = L("update_channel.section.channel"))

        val channels = listOf(Channel.RELEASE, Channel.BETA, Channel.CUSTOM)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            channels.forEach { channel ->
                val isSelected = channel == currentChannel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) colorScheme.secondaryContainer else colorScheme.surfaceContainer)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = colorScheme.primary),
                            onClick = { viewModel.selectChannel(channel) }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = L("update_channel.channel.${channel.name.lowercase()}"),
                            color = if (isSelected) colorScheme.onSecondaryContainer else colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = L("update_channel.channel.${channel.name.lowercase()}.description"),
                            color = if (isSelected) colorScheme.onSecondaryContainer.copy(alpha = 0.8f) else colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        if (currentChannel == Channel.CUSTOM) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = customUrl,
                onValueChange = { viewModel.setCustomUrl(it) },
                label = { Text(L("update_channel.custom_url.title"), color = colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outlineVariant,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface
                ),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Frequency Settings
        SettingsSection(title = L("update_channel.section.frequency"))

        val coreIntervalLabel = coreIntervals.find { it.first == updateInterval }?.second
            ?: L("select_field.select")
        NavSettingsItem(
            title = L("update_channel.core_frequency.title"),
            description = coreIntervalLabel,
            isFirst = true,
            onClick = { showCoreIntervalDialog = true }
        )

        val externalIntervalLabel = externalIntervals.find { it.first == externalInterval }?.second
            ?: L("select_field.select")
        NavSettingsItem(
            title = L("update_channel.external_frequency.title"),
            description = externalIntervalLabel,
            isLast = true,
            onClick = { showExternalIntervalDialog = true }
        )
    }
}
