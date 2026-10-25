package io.github.movetakeitblue.iitcexpressive.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.movetakeitblue.iitcexpressive.i18n.L
import io.github.movetakeitblue.iitcexpressive.theme.*
import io.github.movetakeitblue.iitcexpressive.ui.common.AlertDialog
import io.github.movetakeitblue.iitcexpressive.viewmodel.BackupViewModel

/**
 * Material 3 Expressive Backup & Restore Screen
 */
@Composable
fun BackupView(
    viewModel: BackupViewModel,
    onBack: () -> Unit
) {
    val isWorking by viewModel.isWorking.collectAsState()
    val isInvalidBackup by viewModel.isInvalidBackup.collectAsState()
    val availableBackup by viewModel.availableBackup.collectAsState()
    val importSettings by viewModel.importSettings.collectAsState()
    val importData by viewModel.importData.collectAsState()
    val importExternal by viewModel.importExternal.collectAsState()
    val exportSettings by viewModel.exportSettings.collectAsState()
    val exportData by viewModel.exportData.collectAsState()
    val exportExternal by viewModel.exportExternal.collectAsState()

    var showSuccessDialog by remember { mutableStateOf(false) }

    if (showSuccessDialog) {
        AlertDialog(
            title = L("backup.restore.success_title"),
            message = L("backup.restore.success_message"),
            onDismiss = {
                showSuccessDialog = false
                onBack()
            }
        )
    }

    SettingsBase(
        title = L("backup.title"),
        onBack = onBack
    ) {
        // Import Section
        SettingsSection(title = L("backup.section.import"))

        ActionSettingsItem(
            title = L("backup.choose_file.title"),
            description = L("backup.choose_file.description"),
            isFirst = true,
            isLast = availableBackup == null,
            onClick = { viewModel.selectFile() }
        )

        if (isInvalidBackup) {
            Text(
                text = L("backup.invalid"),
                color = StateError,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(8.dp)
            )
        }

        if (availableBackup != null) {
            val manifest = availableBackup!!
            if (manifest.hasSettings) {
                SwitchSettingsItem(
                    title = L("backup.import_settings.title"),
                    description = L("backup.import_settings.description"),
                    checked = importSettings,
                    onCheckedChange = { viewModel.setImportSettings(it) }
                )
            }
            if (manifest.hasData) {
                SwitchSettingsItem(
                    title = L("backup.import_data.title"),
                    description = L("backup.import_data.description"),
                    checked = importData,
                    onCheckedChange = { viewModel.setImportData(it) }
                )
            }
            if (manifest.hasExternal) {
                SwitchSettingsItem(
                    title = L("backup.import_external.title"),
                    description = L("backup.import_external.description"),
                    checked = importExternal,
                    onCheckedChange = { viewModel.setImportExternal(it) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ActionSettingsItem(
                title = L("backup.restore.title"),
                description = L("backup.restore.description"),
                bold = true,
                isFirst = true,
                isLast = true,
                onClick = {
                    viewModel.executeRestore {
                        showSuccessDialog = true
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Export Section
        SettingsSection(title = L("backup.section.export"))

        SwitchSettingsItem(
            title = L("backup.export_settings.title"),
            description = L("backup.export_settings.description"),
            checked = exportSettings,
            onCheckedChange = { viewModel.setExportSettings(it) },
            isFirst = true
        )
        SwitchSettingsItem(
            title = L("backup.export_data.title"),
            description = L("backup.export_data.description"),
            checked = exportData,
            onCheckedChange = { viewModel.setExportData(it) }
        )
        SwitchSettingsItem(
            title = L("backup.export_external.title"),
            description = L("backup.export_external.description"),
            checked = exportExternal,
            onCheckedChange = { viewModel.setExportExternal(it) },
            isLast = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        ActionSettingsItem(
            title = L("backup.export.title"),
            description = L("backup.export.description"),
            bold = true,
            isFirst = true,
            isLast = true,
            onClick = { viewModel.executeExport() }
        )
    }
}
