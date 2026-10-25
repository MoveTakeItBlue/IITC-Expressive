package org.exarhteam.iitcprime.ui.panel

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.exarhteam.iitcprime.i18n.L

/**
 * Material 3 Expressive Restore Panel FAB
 */
@Composable
fun BoxScope.RestorePanelButton(
    modifier: Modifier = Modifier,
    onRestore: () -> Unit
) {
    FloatingActionButton(
        onClick = onRestore,
        modifier = modifier
            .align(Alignment.BottomEnd)
            .padding(20.dp),
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = L("panel.show"),
            modifier = Modifier.size(26.dp)
        )
    }
}
