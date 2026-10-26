package io.github.movetakeitblue.iitcexpressive.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.movetakeitblue.iitcexpressive.viewmodel.MainViewModel

@Composable
actual fun NativeMapView(
    viewModel: MainViewModel,
    modifier: Modifier
) {
    // iOS placeholder: map rendering is handled by WebView or native iOS MapLibre in future updates
    Box(modifier = modifier)
}
