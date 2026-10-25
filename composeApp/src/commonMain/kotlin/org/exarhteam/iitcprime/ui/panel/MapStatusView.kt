package org.exarhteam.iitcprime.ui.panel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.model.MapStatus
import org.exarhteam.iitcprime.theme.OnSurfaceVariantMuted
import org.exarhteam.iitcprime.theme.OnSurfaceWhite
import org.exarhteam.iitcprime.theme.StateError

@Composable
fun MapStatusView(
    mapStatus: MapStatus,
    modifier: Modifier = Modifier
) {
    val portalLevels = mapStatus.portalLevels
    val portalLevelsText = when {
        portalLevels.hasPortals -> L("map_status.portals")
        portalLevels.minLinkLength > 0 -> ">${portalLevels.formattedLength}"
        else -> L("map_status.all_links")
    }

    val statusDetails = mapStatus.mapStatus
    val requests = mapStatus.requests

    val statusAnnotated = buildAnnotatedString {
        val short = statusDetails.short ?: L("common.loading")
        append(short)

        if (statusDetails.progressPercent != null && statusDetails.progressPercent < 100) {
            append(" ${statusDetails.progressPercent}%")
        }

        if (requests.hasActive || requests.hasFailed) {
            append(" [${requests.active}")
            if (requests.hasFailed) {
                append("/")
                withStyle(SpanStyle(color = StateError)) {
                    append("${requests.failed}")
                }
            }
            append("]")
        }
    }

    Column(
        modifier = modifier.height(38.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = portalLevelsText,
            color = OnSurfaceWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
        Text(
            text = statusAnnotated,
            color = OnSurfaceVariantMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.End
        )
    }
}
