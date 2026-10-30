package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.scoreboard.ScoreboardSheet

class ScoreboardPlugin : IitcNativePlugin {
    override val id: String = "scoreboard"
    override val name: String = "Scoreboard"
    override val description: String = "Regional scoreboard, checkpoint timeline, septicycle countdown, and top agents."
    override val icon: ImageVector = Icons.Default.Leaderboard
    override val category: String = "Info"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        ScoreboardSheet(host = host, onDismiss = onDismiss)
    }
}
