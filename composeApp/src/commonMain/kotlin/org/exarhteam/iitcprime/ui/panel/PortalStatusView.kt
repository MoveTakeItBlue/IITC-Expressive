package org.exarhteam.iitcprime.ui.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.model.PortalStatus
import org.exarhteam.iitcprime.theme.*

@Composable
fun PortalStatusView(
    portalStatus: PortalStatus,
    isWebviewLoaded: Boolean,
    isIitcLoaded: Boolean,
    modifier: Modifier = Modifier
) {
    if (!portalStatus.hasSelected) {
        val message = when {
            !isWebviewLoaded -> L("portal_status.loading")
            !isIitcLoaded -> L("portal_status.sign_in_required")
            else -> L("portal_status.no_portal_selected")
        }
        Box(
            modifier = modifier.height(38.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = message,
                color = OnSurfaceVariantMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        return
    }

    Row(
        modifier = modifier.height(38.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Team color bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(portalStatus.teamColor)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level badge
                val levelBg = parseHexColor(portalStatus.levelColor) ?: Color(0xFF808080)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(levelBg)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "L${portalStatus.level}",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Health percentage
                Text(
                    text = "${portalStatus.health}%",
                    color = OnSurfaceWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Title
                Text(
                    text = portalStatus.title,
                    color = OnSurfaceWhite,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Resonators grid: 8 bars
            val resonators = portalStatus.resonators
            Row(
                modifier = Modifier.fillMaxWidth().height(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (i in 0 until 8) {
                    val resonator = resonators?.getOrNull(i)
                    val resColor = if (resonator != null) parseHexColor(resonator.levelColor) ?: AccentTeal else Color.Transparent
                    val healthPct = resonator?.healthPct ?: 0

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color(0xFF20353A))
                    ) {
                        if (resonator != null && resonator.energy > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = (healthPct.coerceIn(0, 100) / 100f))
                                    .background(resColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun parseHexColor(colorStr: String?): Color? {
    if (colorStr.isNullOrBlank()) return null
    return try {
        val clean = colorStr.trim().removePrefix("#")
        val colorInt = when (clean.length) {
            6 -> clean.toLong(16) or 0x00000000FF000000L
            8 -> clean.toLong(16)
            else -> return null
        }
        Color(colorInt)
    } catch (e: Exception) {
        null
    }
}
