package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.fieldplanner.FieldPlannerSheet
import io.github.movetakeitblue.iitcexpressive.plugin.ui.fieldplanner.PlannerMode

class TidyLinksPlugin : IitcNativePlugin {
    override val id: String = "tidy-links"
    override val name: String = "Tidy Links"
    override val description: String = "Calculates maximum field count connecting portals with shortest links first."
    override val icon: ImageVector = Icons.Default.Polyline
    override val category: String = "Draw"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        FieldPlannerSheet(host = host, initialMode = PlannerMode.TIDY_LINKS, onDismiss = onDismiss)
    }
}
