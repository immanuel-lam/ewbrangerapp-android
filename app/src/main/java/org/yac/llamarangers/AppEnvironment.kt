package org.yac.llamarangers

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.yac.llamarangers.demo.DemoSeeder
import org.yac.llamarangers.service.auth.AuthManager
import org.yac.llamarangers.service.location.LocationManager
import org.yac.llamarangers.service.map.OfflineTileManager
import org.yac.llamarangers.service.sync.MeshSyncEngine
import org.yac.llamarangers.service.sync.PhotoUploadManager
import org.yac.llamarangers.service.sync.SyncEngine
import org.yac.llamarangers.service.sync.SyncQueueManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppEnvironment @Inject constructor(
    val authManager: AuthManager,
    val locationManager: LocationManager,
    val syncEngine: SyncEngine,
    val meshSyncEngine: MeshSyncEngine,
    val syncQueueManager: SyncQueueManager,
    val photoUploadManager: PhotoUploadManager,
    val offlineTileManager: OfflineTileManager,
    private val demoSeeder: DemoSeeder
) {
    companion object {
        private const val TAG = "AppEnvironment"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize() {
        Log.d(TAG, "Initializing AppEnvironment")

        syncEngine.startMonitoring()

        scope.launch {
            try {
                // Seed demo rangers first (required before seed())
                demoSeeder.seedRangersIfNeeded()

                // Set default PIN "1234" if none stored yet
                authManager.initDefaultPinIfNeeded()

                // Seed full demo data (zones, sightings, tasks, pesticides, patrols)
                demoSeeder.seed()

                // Validate restored session: if stored ranger no longer exists, log out
                authManager.validateRestoredSession()

                Log.d(TAG, "App initialization complete")
            } catch (e: Exception) {
                Log.e(TAG, "Initialization error", e)
            }
        }
    }
}
