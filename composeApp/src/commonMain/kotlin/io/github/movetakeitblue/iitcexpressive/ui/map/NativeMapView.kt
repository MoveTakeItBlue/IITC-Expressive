package io.github.movetakeitblue.iitcexpressive.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.movetakeitblue.iitcexpressive.viewmodel.MainViewModel

@Composable
expect fun NativeMapView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
)
