package io.github.movetakeitblue.iitcexpressive.plugin.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcNativePlugin
import io.github.movetakeitblue.iitcexpressive.plugin.api.IitcPluginHost
import io.github.movetakeitblue.iitcexpressive.plugin.ui.playerlevel.PlayerLevelGuessSheet

class PlayerLevelGuessPlugin : IitcNativePlugin {
    override val id: String = "player-level-guess"
    override val name: String = "Player Level Guess"
    override val description: String = "Infers player levels and faction from COMM chat activities and deployed portal resonators."
    override val icon: ImageVector = Icons.Default.Badge
    override val category: String = "Info"

    @Composable
    override fun SheetContent(host: IitcPluginHost, onDismiss: () -> Unit) {
        PlayerLevelGuessSheet(host = host, onDismiss = onDismiss)
    }
}
