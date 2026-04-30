package org.yac.llamarangers.ui.more

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.local.dao.RangerProfileDao
import org.yac.llamarangers.data.local.dao.SyncQueueDao
import org.yac.llamarangers.service.auth.AuthManager
import org.yac.llamarangers.service.map.OfflineTileManager
import org.yac.llamarangers.service.sync.SyncEngine
import javax.inject.Inject

/**
 * Ports iOS SettingsViewModel.
 * Manages ranger profile, sync status, field conditions, offline maps, and app settings.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val syncEngine: SyncEngine,
    private val syncQueueDao: SyncQueueDao,
    private val rangerProfileDao: RangerProfileDao,
    private val offlineTileManager: OfflineTileManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        const val RECENT_RAIN_KEY = "recentRainFlagged"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val _currentRangerName = MutableStateFlow("")
    val currentRangerName: StateFlow<String> = _currentRangerName.asStateFlow()

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    private val _lastSyncDate = MutableStateFlow<Long?>(null)
    val lastSyncDate: StateFlow<Long?> = _lastSyncDate.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _recentRainFlagged = MutableStateFlow(false)
    val recentRainFlagged: StateFlow<Boolean> = _recentRainFlagged.asStateFlow()

    val tileStatus: StateFlow<OfflineTileManager.TileStatus> = offlineTileManager.tileStatus

    private val _pinChangeError = MutableStateFlow<String?>(null)
    val pinChangeError: StateFlow<String?> = _pinChangeError.asStateFlow()

    private val _pinChangeSuccess = MutableStateFlow(false)
    val pinChangeSuccess: StateFlow<Boolean> = _pinChangeSuccess.asStateFlow()

    init {
        _recentRainFlagged.value = prefs.getBoolean(RECENT_RAIN_KEY, false)
        loadProfile()
        refreshSyncStatus()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val rangerId = authManager.currentRangerId.value?.toString() ?: return@launch
            val ranger = rangerProfileDao.findById(rangerId)
            _currentRangerName.value = ranger?.displayName ?: ""
        }
    }

    fun updateDisplayName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val rangerId = authManager.currentRangerId.value?.toString() ?: return

        viewModelScope.launch {
            rangerProfileDao.updateDisplayName(rangerId, trimmed, System.currentTimeMillis())
            _currentRangerName.value = trimmed
        }
    }

    fun changePIN(oldPIN: String, newPIN: String, confirmPIN: String) {
        _pinChangeError.value = null
        _pinChangeSuccess.value = false

        if (newPIN != confirmPIN) {
            _pinChangeError.value = "New PINs don't match."
            return
        }
        if (newPIN.length < 4) {
            _pinChangeError.value = "PIN must be at least 4 digits."
            return
        }
        if (!authManager.changePIN(oldPIN, newPIN)) {
            _pinChangeError.value = "Current PIN is incorrect."
            return
        }
        _pinChangeSuccess.value = true
    }

    fun toggleRecentRain() {
        val newValue = !_recentRainFlagged.value
        _recentRainFlagged.value = newValue
        prefs.edit().putBoolean(RECENT_RAIN_KEY, newValue).apply()
    }

    fun syncNow() {
        if (_isSyncing.value) return
        _isSyncing.value = true

        viewModelScope.launch {
            try {
                // Fake a realistic 2-3s sync round-trip for the demo
                delay((2000L..3200L).random())
                val syncPrefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
                syncPrefs.edit().putLong("lastSyncTimestamp", System.currentTimeMillis()).apply()
                refreshSyncStatus()
            } catch (_: Exception) {
                // Sync failed; user can retry
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun logout() {
        authManager.logout()
    }

    private fun refreshSyncStatus() {
        viewModelScope.launch {
            _pendingSyncCount.value = syncQueueDao.count()
            val syncPrefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
            val lastSync = syncPrefs.getLong("lastSyncTimestamp", -1L)
            _lastSyncDate.value = if (lastSync >= 0) lastSync else null
        }
    }

    val appVersion: String
        get() = try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName ?: "1.0"
        } catch (_: PackageManager.NameNotFoundException) {
            "1.0"
        }
}
