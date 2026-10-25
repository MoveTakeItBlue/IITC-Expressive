package org.exarhteam.iitcprime.ui.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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

fun parseHexColor(hex: String?): Color? {
    if (hex == null || !hex.startsWith("#")) return null
    return try {
        val clean = hex.removePrefix("#")
        when (clean.length) {
            6 -> Color(clean.toLong(16) or 0x00000000FF000000)
            8 -> Color(clean.toLong(16))
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

/**
 * Material 3 Expressive PortalStatusView
 */
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
            modifier = modifier.fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        return
    }

    Row(
        modifier = modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Team color capsule
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(portalStatus.teamColor)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Level, health & title row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level badge (M3 Expressive 6dp pill)
                val levelBg = parseHexColor(portalStatus.levelColor) ?: Color(0xFF808080)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(levelBg)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "L${portalStatus.level}",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Health percentage
                Text(
                    text = "${portalStatus.health}%",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Title
                Text(
                    text = portalStatus.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Resonators grid (8 expressive slots)
            Row(
                modifier = Modifier.fillMaxWidth().height(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val resonators = portalStatus.resonators
                val defaultResColor = MaterialTheme.colorScheme.primary
                for (i in 0 until 8) {
                    val res = resonators?.getOrNull(i)
                    val healthPct = res?.healthPct ?: 0
                    val pct = (healthPct.toFloat() / 100f).coerceIn(0f, 1f)
                    val resColor = parseHexColor(res?.levelColor) ?: defaultResColor

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        if (pct > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(pct)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(resColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
