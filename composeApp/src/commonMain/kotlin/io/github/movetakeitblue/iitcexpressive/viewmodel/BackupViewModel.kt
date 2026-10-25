package io.github.movetakeitblue.iitcexpressive.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.github.movetakeitblue.iitcexpressive.manager.BackupManager
import io.github.movetakeitblue.iitcexpressive.model.BackupInspection
import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils

class BackupViewModel(
    private val backupManager: BackupManager
) {
    // Import state
    private val _pickedBytes = MutableStateFlow<ByteArray?>(null)
    val pickedBytes: StateFlow<ByteArray?> = _pickedBytes.asStateFlow()

    private val _inspection = MutableStateFlow<BackupInspection?>(null)
    val inspection: StateFlow<BackupInspection?> = _inspection.asStateFlow()

    private val _isInvalidBackup = MutableStateFlow(false)
    val isInvalidBackup: StateFlow<Boolean> = _isInvalidBackup.asStateFlow()

    private val _isWorking = MutableStateFlow(false)
    val isWorking: StateFlow<Boolean> = _isWorking.asStateFlow()

    val availableBackup: StateFlow<BackupInspection?> = inspection

    private val _importSettings = MutableStateFlow(true)
    val importSettings: StateFlow<Boolean> = _importSettings.asStateFlow()

    private val _importData = MutableStateFlow(true)
    val importData: StateFlow<Boolean> = _importData.asStateFlow()

    private val _importExternal = MutableStateFlow(true)
    val importExternal: StateFlow<Boolean> = _importExternal.asStateFlow()

    // Export state
    private val _exportSettings = MutableStateFlow(true)
    val exportSettings: StateFlow<Boolean> = _exportSettings.asStateFlow()

    private val _exportData = MutableStateFlow(true)
    val exportData: StateFlow<Boolean> = _exportData.asStateFlow()

    private val _exportExternal = MutableStateFlow(true)
    val exportExternal: StateFlow<Boolean> = _exportExternal.asStateFlow()

    fun onFilePicked(bytes: ByteArray) {
        _pickedBytes.value = bytes
        val ins = backupManager.inspectBackup(bytes)
        if (!ins.hasSettings && !ins.hasData && !ins.hasExternal) {
            _isInvalidBackup.value = true
            _inspection.value = null
        } else {
            _isInvalidBackup.value = false
            _inspection.value = ins
            _importSettings.value = ins.hasSettings
            _importData.value = ins.hasData
            _importExternal.value = ins.hasExternal
        }
    }

    fun setImportSettings(enabled: Boolean) { _importSettings.value = enabled }
    fun setImportData(enabled: Boolean) { _importData.value = enabled }
    fun setImportExternal(enabled: Boolean) { _importExternal.value = enabled }

    fun setExportSettings(enabled: Boolean) { _exportSettings.value = enabled }
    fun setExportData(enabled: Boolean) { _exportData.value = enabled }
    fun setExportExternal(enabled: Boolean) { _exportExternal.value = enabled }

    fun selectFile() {
        val bytes = PlatformUtils.pickFile() ?: return
        onFilePicked(bytes)
    }

    fun executeRestore(onDone: () -> Unit) {
        _isWorking.value = true
        performRestore {
            _isWorking.value = false
            onDone()
        }
    }

    fun executeExport() {
        _isWorking.value = true
        performExport {
            _isWorking.value = false
        }
    }

    fun performRestore(onResult: (Boolean) -> Unit) {
        val bytes = _pickedBytes.value ?: return onResult(false)
        val success = backupManager.restoreBackup(
            bytes = bytes,
            importSettings = _importSettings.value,
            importData = _importData.value,
            importExternal = _importExternal.value
        )
        onResult(success)
    }

    fun performExport(onResult: (Boolean) -> Unit) {
        try {
            val bytes = backupManager.exportBackup(
                exportSettings = _exportSettings.value,
                exportData = _exportData.value,
                exportExternal = _exportExternal.value
            )
            val now = PlatformUtils.currentTimeMillis()
            val filename = "iitc-backup_$now.zip"
            PlatformUtils.shareFile(filename, bytes, "application/zip")
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }
}
