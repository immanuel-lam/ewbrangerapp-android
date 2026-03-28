package org.yac.llamarangers.service.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloud sync engine (V2 stub -- no-op).
 * Monitors network connectivity via ConnectivityManager.NetworkCallback.
 * Ports iOS SyncEngine actor.
 */
@Singleton
class SyncEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SyncEngine"
        private const val LAST_SYNC_KEY = "lastSyncTimestamp"
    }

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    // --- Network monitoring ---

    fun startMonitoring() {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val wasOffline = !_isOnline.value
                _isOnline.value = true
                if (wasOffline) {
                    triggerSync()
                }
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
            }
        }

        networkCallback = callback
        connectivityManager.registerNetworkCallback(request, callback)
    }

    fun stopMonitoring() {
        networkCallback?.let {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(it)
        }
        networkCallback = null
    }

    // --- Sync trigger ---

    /**
     * PoC: cloud sync is stubbed out -- no Supabase backend required.
     * All data lives locally in Room. Mesh sync (Nearby Connections) handles
     * device-to-device sync without any internet or paid services.
     */
    fun triggerSync() {
        Log.d(TAG, "Cloud sync disabled for PoC -- data is local only.")
    }

    // --- Status ---

    val lastSyncDate: Date?
        get() {
            val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
            val timestamp = prefs.getLong(LAST_SYNC_KEY, -1L)
            return if (timestamp >= 0) Date(timestamp) else null
        }
}
