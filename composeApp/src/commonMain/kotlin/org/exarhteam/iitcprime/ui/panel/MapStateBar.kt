package org.exarhteam.iitcprime.ui.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.exarhteam.iitcprime.model.MapStatus
import org.exarhteam.iitcprime.model.PortalStatus
import org.exarhteam.iitcprime.theme.SurfaceContainerDark

@Composable
fun MapStateBar(
    portalStatus: PortalStatus,
    mapStatus: MapStatus,
    isWebviewLoaded: Boolean,
    isIitcLoaded: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(SurfaceContainerDark)
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PortalStatusView(
                portalStatus = portalStatus,
                isWebviewLoaded = isWebviewLoaded,
                isIitcLoaded = isIitcLoaded,
                modifier = Modifier.weight(if (isIitcLoaded) 0.65f else 1.0f)
            )

            if (isIitcLoaded) {
                MapStatusView(
                    mapStatus = mapStatus,
                    modifier = Modifier.weight(0.35f)
                )
            }
        }
    }
}

@Composable
fun MapStateBar(
    viewModel: org.exarhteam.iitcprime.viewmodel.MainViewModel,
    modifier: Modifier = Modifier
) {
    val portalStatus by viewModel.portalStatus.collectAsState()
    val mapStatus by viewModel.mapStatus.collectAsState()
    val isWebviewLoaded by viewModel.isWebviewLoaded.collectAsState()
    val isIitcLoaded by viewModel.isIitcLoaded.collectAsState()

    MapStateBar(
        portalStatus = portalStatus,
        mapStatus = mapStatus,
        isWebviewLoaded = isWebviewLoaded,
        isIitcLoaded = isIitcLoaded,
        onTap = { viewModel.togglePanel() },
        modifier = modifier
    )
}
