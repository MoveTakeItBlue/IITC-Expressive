package io.github.movetakeitblue.iitcexpressive.plugin.ui.apstats

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.movetakeitblue.iitcexpressive.model.NativePortal
import io.github.movetakeitblue.iitcexpressive.plugin.api.CycleTimesCalculator
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.theme.*

data class FactionApStats(
    val destroyResonatorsAp: Long = 0,
    val destroyLinksAp: Long = 0,
    val destroyFieldsAp: Long = 0,
    val capturePortalAp: Long = 0,
    val deployResonatorsAp: Long = 0
) {
    val totalAp: Long
        get() = destroyResonatorsAp + destroyLinksAp + destroyFieldsAp + capturePortalAp + deployResonatorsAp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApStatsSheet(
    host: IitcPluginHost,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val portals by host.visiblePortals.collectAsState()
    val links by host.visibleLinks.collectAsState()
    val fields by host.visibleFields.collectAsState()

    // Calculate AP breakdown for Resistance and Enlightened
    val (resAp, enlAp) = remember(portals, links, fields) {
        calculateApBreakdown(portals, links.size, fields.size)
    }

    val totalCombinedAp = (resAp.totalAp + enlAp.totalAp).coerceAtLeast(1L)
    val resRatio = resAp.totalAp.toFloat() / totalCombinedAp.toFloat()

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
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Available AP Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${portals.size} portals • ${links.size} links • ${fields.size} fields in view",
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Overview Summary Card
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "RES AP GAIN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ResBlue
                                    )
                                    Text(
                                        text = CycleTimesCalculator.formatNumber(resAp.totalAp),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ResBlue
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "ENL AP GAIN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EnlGreen
                                    )
                                    Text(
                                        text = CycleTimesCalculator.formatNumber(enlAp.totalAp),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EnlGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val animatedRatio by animateFloatAsState(targetValue = resRatio)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(animatedRatio.coerceAtLeast(0.01f))
                                        .background(ResBlue)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight((1f - animatedRatio).coerceAtLeast(0.01f))
                                        .background(EnlGreen)
                                )
                            }
                        }
                    }
                }

                // Portal counts chips
                item {
                    val neutralCount = portals.count { it.team == 0 }
                    val resCount = portals.count { it.team == 1 }
                    val enlCount = portals.count { it.team == 2 }
                    val macCount = portals.count { it.team == 3 }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FactionCountChip(label = "RES", count = resCount, color = ResBlue, modifier = Modifier.weight(1f))
                        FactionCountChip(label = "ENL", count = enlCount, color = EnlGreen, modifier = Modifier.weight(1f))
                        FactionCountChip(label = "MAC", count = macCount, color = MacRed, modifier = Modifier.weight(1f))
                        FactionCountChip(label = "NEU", count = neutralCount, color = NeutralOrange, modifier = Modifier.weight(1f))
                    }
                }

                // Breakdown section
                item {
                    Text(
                        text = "AP BREAKDOWN BY ACTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                item {
                    ApActionCard(
                        title = "Neutralize & Capture",
                        subtitle = "Destroy enemy portals & claim unclaimed",
                        icon = Icons.Default.Flag,
                        resAp = resAp.capturePortalAp,
                        enlAp = enlAp.capturePortalAp
                    )
                }

                item {
                    ApActionCard(
                        title = "Resonators (Deploy & Destroy)",
                        subtitle = "75 AP destroy / 125 AP deploy per slot",
                        icon = Icons.Default.Bolt,
                        resAp = resAp.destroyResonatorsAp + resAp.deployResonatorsAp,
                        enlAp = enlAp.destroyResonatorsAp + enlAp.deployResonatorsAp
                    )
                }

                item {
                    ApActionCard(
                        title = "Links & Fields Destruction",
                        subtitle = "187 AP per link / 750 AP per field",
                        icon = Icons.Default.Share,
                        resAp = resAp.destroyLinksAp + resAp.destroyFieldsAp,
                        enlAp = enlAp.destroyLinksAp + enlAp.destroyFieldsAp
                    )
                }
            }
        }
    }
}

@Composable
private fun FactionCountChip(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun ApActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    resAp: Long,
    enlAp: Long
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${CycleTimesCalculator.formatNumber(resAp)} AP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = ResBlue
                )
                Text(
                    text = "${CycleTimesCalculator.formatNumber(enlAp)} AP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EnlGreen
                )
            }
        }
    }
}

private fun calculateApBreakdown(
    portals: List<NativePortal>,
    linksCount: Int,
    fieldsCount: Int
): Pair<FactionApStats, FactionApStats> {
    var resNeutralize = 0L
    var resDestroyRes = 0L
    var resDeploy = 0L

    var enlNeutralize = 0L
    var enlDestroyRes = 0L
    var enlDeploy = 0L

    portals.forEach { p ->
        val resCount = p.resCount.coerceIn(0, 8)
        when (p.team) {
            0 -> {
                // Neutral portal: both can capture (500) and deploy 8 resonators (8*125 = 1000)
                resNeutralize += 500L
                resDeploy += 1000L
                enlNeutralize += 500L
                enlDeploy += 1000L
            }
            1 -> {
                // RES portal: ENL can destroy resonators, neutralize, and deploy
                enlDestroyRes += (resCount * 75L)
                enlNeutralize += 500L
                enlDeploy += 1000L
                // RES can fill missing resonators
                resDeploy += ((8 - resCount) * 125L)
            }
            2 -> {
                // ENL portal: RES can destroy resonators, neutralize, and deploy
                resDestroyRes += (resCount * 75L)
                resNeutralize += 500L
                resDeploy += 1000L
                // ENL can fill missing resonators
                enlDeploy += ((8 - resCount) * 125L)
            }
            3 -> {
                // Machina portal: both can destroy resonators, neutralize, and deploy
                resDestroyRes += (resCount * 75L)
                resNeutralize += 500L
                resDeploy += 1000L

                enlDestroyRes += (resCount * 75L)
                enlNeutralize += 500L
                enlDeploy += 1000L
            }
        }
    }

    // Estimate links and fields destruction AP (proportional to portals)
    val resDestroyLinks = (linksCount * 187L) / 2L
    val enlDestroyLinks = (linksCount * 187L) / 2L
    val resDestroyFields = (fieldsCount * 750L) / 2L
    val enlDestroyFields = (fieldsCount * 750L) / 2L

    val resStats = FactionApStats(
        destroyResonatorsAp = resDestroyRes,
        destroyLinksAp = resDestroyLinks,
        destroyFieldsAp = resDestroyFields,
        capturePortalAp = resNeutralize,
        deployResonatorsAp = resDeploy
    )

    val enlStats = FactionApStats(
        destroyResonatorsAp = enlDestroyRes,
        destroyLinksAp = enlDestroyLinks,
        destroyFieldsAp = enlDestroyFields,
        capturePortalAp = enlNeutralize,
        deployResonatorsAp = enlDeploy
    )

    return Pair(resStats, enlStats)
}
