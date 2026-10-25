package org.exarhteam.iitcprime.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.theme.Accent
import org.exarhteam.iitcprime.theme.SurfaceContainer
import org.exarhteam.iitcprime.viewmodel.PluginsViewModel

@Composable
fun PluginsView(
    viewModel: PluginsViewModel,
    onBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeCategory by viewModel.activeCategory.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val filteredPlugins by viewModel.filteredPlugins.collectAsState()
    val pendingPlugin by viewModel.pendingPlugin.collectAsState()
    val selectedPluginForInfo by viewModel.selectedPluginForInfo.collectAsState()
    val showAddPluginSheet by viewModel.showAddPluginSheet.collectAsState()

    // Add Plugin Sheet
    if (showAddPluginSheet) {
        AddPluginSheet(
            onLoadUrl = { url ->
                viewModel.loadPluginFromUrl(url)
            },
            onChooseFile = {
                viewModel.loadPluginFromFile()
            },
            onDismiss = {
                viewModel.dismissAddPlugin()
            }
        )
    }

    // Pending Plugin Install Sheet
    if (pendingPlugin != null) {
        PluginInfoSheet(
            plugin = pendingPlugin!!,
            installMode = true,
            onInstall = {
                viewModel.installPendingPlugin()
            },
            onDismiss = {
                viewModel.dismissPluginInfo()
            }
        )
    }

    // Selected Plugin Info Sheet
    if (selectedPluginForInfo != null) {
        PluginInfoSheet(
            plugin = selectedPluginForInfo!!,
            installMode = false,
            onDelete = if (selectedPluginForInfo!!.user || selectedPluginForInfo!!.override) {
                {
                    viewModel.deletePlugin(selectedPluginForInfo!!)
                    viewModel.dismissPluginInfo()
                }
            } else null,
            onDismiss = {
                viewModel.dismissPluginInfo()
            }
        )
    }

    SettingsBase(
        title = L("settings.plugins.title"),
        onBack = onBack,
        search = true,
        searchText = searchQuery,
        onSearchTextChange = { viewModel.setSearchQuery(it) },
        searchHint = L("plugins.search_hint"),
        useScroll = false,
        headerRight = {
            TextButton(onClick = { viewModel.showAddPlugin() }) {
                Text(
                    text = L("plugins.add_plugin"),
                    color = Accent,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) {
        // Horizontal Categories List
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = category == activeCategory
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) Accent else SurfaceContainer)
                        .clickable { viewModel.setActiveCategory(category) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Plugins List
        if (filteredPlugins.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = L("plugins.no_plugins"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                itemsIndexed(filteredPlugins, key = { _, item -> item.id }) { index, plugin ->
                    val isFirst = index == 0
                    val isLast = index == filteredPlugins.size - 1

                    PluginItem(
                        plugin = plugin,
                        isFirst = isFirst,
                        isLast = isLast,
                        onClick = { viewModel.showPluginInfo(plugin) },
                        onToggle = { viewModel.togglePlugin(plugin) },
                        onDelete = if (plugin.user || plugin.override) {
                            { viewModel.deletePlugin(plugin) }
                        } else null
                    )
                }
            }
        }
    }
}
