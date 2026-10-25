package io.github.movetakeitblue.iitcexpressive.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.github.movetakeitblue.iitcexpressive.i18n.L
import io.github.movetakeitblue.iitcexpressive.manager.IITCManager
import io.github.movetakeitblue.iitcexpressive.model.Channel
import io.github.movetakeitblue.iitcexpressive.platform.PlatformUtils
import io.github.movetakeitblue.iitcexpressive.storage.AppStorage

class UpdateChannelViewModel(
    val manager: IITCManager,
    private val storage: AppStorage
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val currentChannel: StateFlow<Channel> = manager.currentChannel
    val customChannelUrl: StateFlow<String> = manager.customChannelUrl
    val isCheckingUpdates: StateFlow<Boolean> = manager.isCheckingUpdates
    val lastCheckTime: StateFlow<Long?> = manager.lastCheckTime

    private val _selectedInterval = MutableStateFlow(storage.getLong("update_interval", 86400L))
    val selectedInterval: StateFlow<Long> = _selectedInterval.asStateFlow()

    private val _selectedExternalInterval = MutableStateFlow(storage.getLong("external_update_interval", 86400L))
    val selectedExternalInterval: StateFlow<Long> = _selectedExternalInterval.asStateFlow()

    fun selectChannel(channel: Channel) {
        manager.setChannel(channel)
    }

    fun setCustomUrl(url: String) {
        manager.setCustomChannelUrl(url)
    }

    fun setUpdateInterval(interval: Long) {
        _selectedInterval.value = interval
        storage.setLong("update_interval", interval)
    }

    fun setExternalUpdateInterval(interval: Long) {
        _selectedExternalInterval.value = interval
        storage.setLong("external_update_interval", interval)
    }

    fun checkUpdatesNow() {
        scope.launch {
            manager.checkUpdates(force = true)
        }
    }

    fun getFormattedLastCheck(now: Long = PlatformUtils.currentTimeMillis()): String {
        val check = lastCheckTime.value ?: return ""
        val diff = (now - check) / 1000
        return when {
            diff < 60 -> L("update_channel.time.just_now")
            diff < 3600 -> L("update_channel.time.minutes_ago", diff / 60)
            diff < 86400 -> L("update_channel.time.hours_ago", diff / 3600)
            else -> L("update_channel.time.days_ago", diff / 86400)
        }
    }
}
