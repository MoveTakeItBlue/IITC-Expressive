package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.fieldplanner.FieldPlannerSheet
import io.github.movetakeitblue.iitcexpressive.plugin.ui.fieldplanner.PlannerMode

class FlyLinksPlugin : IitcNativePlugin {
    override val id: String = "fly-links"
    override val name: String = "Fly Links"
    override val description: String = "Calculates optimal fields connecting portals with longest links first (outer perimeter inward)."
    override val icon: ImageVector = Icons.Default.AutoAwesome
    override val category: String = "Draw"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        FieldPlannerSheet(host = host, initialMode = PlannerMode.FLY_LINKS, onDismiss = onDismiss)
    }
}
