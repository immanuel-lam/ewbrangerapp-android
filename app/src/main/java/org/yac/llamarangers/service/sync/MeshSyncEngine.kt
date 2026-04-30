package org.yac.llamarangers.service.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manifest entry for mesh sync exchange.
 */
data class ManifestEntry(
    val entityName: String,
    val id: String,
    val updatedAt: Long // epoch millis for easy comparison
)

/**
 * Device-to-device mesh sync using Google Nearby Connections API.
 * Ports iOS MeshSyncEngine (MultipeerConnectivity).
 *
 * Mapping:
 *   MCSession -> ConnectionsClient
 *   MCNearbyServiceAdvertiser -> startAdvertising
 *   MCNearbyServiceBrowser -> startDiscovery
 *   service type "yac-lantana" -> service ID "org.yac.llamarangers"
 */
@Singleton
class MeshSyncEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "MeshSyncEngine"
        private const val SERVICE_ID = "org.yac.llamarangers"
    }

    sealed class SyncPhase {
        data object Idle : SyncPhase()
        data object Discovering : SyncPhase()
        data class Connected(val peerName: String) : SyncPhase()
        data class Syncing(val peerName: String, val progress: Double) : SyncPhase()
        data class Done(val peerName: String, val sent: Int, val received: Int) : SyncPhase()
        data class Failed(val peerName: String, val error: String) : SyncPhase()
    }

    data class DiscoveredPeer(
        val endpointId: String,
        val name: String
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private val gson = Gson()

    private val connectionsClient: ConnectionsClient by lazy {
        Nearby.getConnectionsClient(context)
    }

    private val _discoveredPeers = MutableStateFlow<List<DiscoveredPeer>>(emptyList())
    val discoveredPeers: StateFlow<List<DiscoveredPeer>> = _discoveredPeers.asStateFlow()

    private val _peerPhases = MutableStateFlow<Map<String, SyncPhase>>(emptyMap())
    val peerPhases: StateFlow<Map<String, SyncPhase>> = _peerPhases.asStateFlow()

    private val _overallPhase = MutableStateFlow<SyncPhase>(SyncPhase.Idle)
    val overallPhase: StateFlow<SyncPhase> = _overallPhase.asStateFlow()

    // Connected endpoint IDs (accessed from Nearby callback threads)
    private val connectedEndpoints: MutableSet<String> =
        Collections.synchronizedSet(mutableSetOf())

    /**
     * Callback to build the local manifest. Set by AppEnvironment or DI.
     * Returns list of ManifestEntry from the Room database.
     */
    var manifestBuilder: (suspend () -> List<ManifestEntry>)? = null

    /**
     * Callback to fetch requested records by IDs. Returns JSON-encoded records list.
     */
    var recordsFetcher: (suspend (ids: List<String>) -> String)? = null

    /**
     * Callback to receive and merge incoming records data.
     */
    var recordsReceiver: (suspend (recordsJson: String) -> Unit)? = null

    private var displayName: String = android.os.Build.MODEL

    // --- Start / Stop ---

    fun start(displayName: String = android.os.Build.MODEL) {
        this.displayName = displayName

        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        connectionsClient.startAdvertising(
            displayName,
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener {
            Log.d(TAG, "Advertising started")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Advertising failed", e)
        }

        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnSuccessListener {
            Log.d(TAG, "Discovery started")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Discovery failed", e)
        }

        _overallPhase.value = SyncPhase.Discovering
    }

    fun stop() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        connectedEndpoints.clear()
        _overallPhase.value = SyncPhase.Idle
        _discoveredPeers.value = emptyList()
        _peerPhases.value = emptyMap()
    }

    // --- Connection lifecycle ---

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Auto-accept from any device in the trusted ranger group (PoC)
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            when (resolution.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d(TAG, "Connected to $endpointId")
                    connectedEndpoints.add(endpointId)
                    val peerName = _discoveredPeers.value
                        .find { it.endpointId == endpointId }?.name ?: endpointId
                    _peerPhases.value = _peerPhases.value + (endpointId to SyncPhase.Connected(peerName))
                    // Send manifest on connect
                    scope.launch { sendManifest(endpointId) }
                }
                else -> {
                    Log.w(TAG, "Connection failed to $endpointId: ${resolution.status}")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "Disconnected from $endpointId")
            connectedEndpoints.remove(endpointId)
            _peerPhases.value = _peerPhases.value - endpointId
        }
    }

    // --- Endpoint discovery ---

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val peer = DiscoveredPeer(endpointId, info.endpointName)
            if (_discoveredPeers.value.none { it.endpointId == endpointId }) {
                _discoveredPeers.value = _discoveredPeers.value + peer
            }
            // Auto-invite discovered peers
            connectionsClient.requestConnection(displayName, endpointId, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
            _discoveredPeers.value = _discoveredPeers.value.filter { it.endpointId != endpointId }
        }
    }

    // --- Payload handling ---

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            val json = String(bytes, Charsets.UTF_8)
            scope.launch { handleReceivedData(json, endpointId) }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // No-op for byte payloads (transferred atomically)
        }
    }

    // --- Manifest ---

    suspend fun buildManifest(): List<ManifestEntry> {
        return manifestBuilder?.invoke() ?: emptyList()
    }

    private suspend fun sendManifest(endpointId: String) {
        try {
            mutex.withLock {
                val manifest = buildManifest()
                val message = mapOf(
                    "type" to "manifest",
                    "entries" to manifest
                )
                val data = gson.toJson(message).toByteArray(Charsets.UTF_8)
                connectionsClient.sendPayload(endpointId, Payload.fromBytes(data))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send manifest to $endpointId", e)
            val peerName = _discoveredPeers.value
                .find { it.endpointId == endpointId }?.name ?: endpointId
            _peerPhases.value = _peerPhases.value +
                (endpointId to SyncPhase.Failed(peerName, e.message ?: "Manifest send failed"))
        }
    }

    // --- Data handling ---

    private suspend fun handleReceivedData(json: String, fromEndpoint: String) {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val parsed: Map<String, Any> = try {
            gson.fromJson(json, mapType)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse incoming data", e)
            return
        }

        val type = parsed["type"] as? String ?: return

        try {
            when (type) {
                "manifest" -> {
                    val entriesJson = gson.toJson(parsed["entries"])
                    val entriesType = object : TypeToken<List<ManifestEntry>>() {}.type
                    val theirEntries: List<ManifestEntry> = gson.fromJson(entriesJson, entriesType)
                    val myEntries = buildManifest()
                    val needed = diffManifest(theirEntries, myEntries)
                    sendRecordRequests(needed, fromEndpoint)
                }
                "request" -> {
                    @Suppress("UNCHECKED_CAST")
                    val ids = (parsed["ids"] as? List<*>)?.filterIsInstance<String>() ?: return
                    sendRequestedRecords(ids, fromEndpoint)
                }
                "records" -> {
                    val recordsJson = gson.toJson(parsed["records"])
                    receiveRecords(recordsJson, fromEndpoint)
                }
                else -> {
                    Log.w(TAG, "Unknown mesh message type: $type")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing mesh message type=$type from $fromEndpoint", e)
            val peerName = _discoveredPeers.value
                .find { it.endpointId == fromEndpoint }?.name ?: fromEndpoint
            _peerPhases.value = _peerPhases.value +
                (fromEndpoint to SyncPhase.Failed(peerName, e.message ?: "Unknown error"))
        }
    }

    /**
     * Computes which entries from theirs are newer or missing from mine.
     */
    fun diffManifest(theirs: List<ManifestEntry>, mine: List<ManifestEntry>): List<ManifestEntry> {
        val myIndex = mine.associate { it.id to it.updatedAt }
        return theirs.filter { entry ->
            val myDate = myIndex[entry.id]
            if (myDate != null) {
                entry.updatedAt > myDate
            } else {
                true // They have it, I don't
            }
        }
    }

    private suspend fun sendRecordRequests(needed: List<ManifestEntry>, toEndpoint: String) {
        if (needed.isEmpty()) return
        val ids = needed.map { it.id }
        val request = mapOf("type" to "request", "ids" to ids)
        val data = gson.toJson(request).toByteArray(Charsets.UTF_8)
        connectionsClient.sendPayload(toEndpoint, Payload.fromBytes(data))
    }

    private suspend fun sendRequestedRecords(ids: List<String>, toEndpoint: String) {
        val recordsJson = recordsFetcher?.invoke(ids) ?: return
        val message = """{"type":"records","records":$recordsJson}"""
        connectionsClient.sendPayload(toEndpoint, Payload.fromBytes(message.toByteArray(Charsets.UTF_8)))
    }

    private suspend fun receiveRecords(recordsJson: String, fromEndpoint: String) {
        recordsReceiver?.invoke(recordsJson)
    }
}
