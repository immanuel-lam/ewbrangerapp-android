package org.yac.llamarangers.ui.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.local.dao.RangerProfileDao
import org.yac.llamarangers.service.auth.AuthManager
import javax.inject.Inject

/**
 * Discovered peer for display in the UI.
 */
data class PeerInfo(
    val name: String,
    val status: String,
    val progress: Double = 0.0
)

/**
 * Phase of the demo mesh sync animation.
 */
enum class SyncPhase {
    IDLE, DISCOVERING, SYNCING, DONE
}

/**
 * Ports iOS DemoMeshSyncView + MeshSyncViewModel.
 * Runs a fake animated sync sequence matching the iOS demo behaviour,
 * discovering the two other rangers who are NOT the currently logged-in ranger.
 */
@HiltViewModel
class MeshSyncViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val rangerProfileDao: RangerProfileDao
) : ViewModel() {

    private val _phase = MutableStateFlow(SyncPhase.IDLE)
    val phase: StateFlow<SyncPhase> = _phase.asStateFlow()

    private val _peers = MutableStateFlow<List<PeerInfo>>(emptyList())
    val peers: StateFlow<List<PeerInfo>> = _peers.asStateFlow()

    private val _showPeers = MutableStateFlow(false)
    val showPeers: StateFlow<Boolean> = _showPeers.asStateFlow()

    private val _summary = MutableStateFlow<String?>(null)
    val summary: StateFlow<String?> = _summary.asStateFlow()

    private var peerNames: List<String> = listOf("Ranger A's Phone", "Ranger B's Phone")

    init {
        loadPeerNames()
    }

    private fun loadPeerNames() {
        viewModelScope.launch {
            val currentId = authManager.currentRangerId.value?.toString()
            val allRangers = rangerProfileDao.fetchAll()
            val others = allRangers
                .filter { it.id != currentId }
                .mapNotNull { it.displayName }
                .sorted()
            peerNames = if (others.size >= 2) {
                listOf("${others[0]}'s Phone", "${others[1]}'s Phone")
            } else {
                listOf("Ranger A's Phone", "Ranger B's Phone")
            }
        }
    }

    val bannerText: String
        get() = when (_phase.value) {
            SyncPhase.IDLE -> "Not syncing"
            SyncPhase.DISCOVERING -> "Searching for nearby rangers..."
            SyncPhase.SYNCING -> "Syncing with nearby devices..."
            SyncPhase.DONE -> "Sync complete \u2014 all records up to date"
        }

    val buttonTitle: String
        get() = when (_phase.value) {
            SyncPhase.IDLE -> "Start Sync"
            SyncPhase.DISCOVERING -> "Searching..."
            SyncPhase.SYNCING -> "Syncing..."
            SyncPhase.DONE -> "Sync Again"
        }

    val isButtonEnabled: Boolean
        get() = _phase.value == SyncPhase.IDLE || _phase.value == SyncPhase.DONE

    fun onButtonTap() {
        if (_phase.value == SyncPhase.IDLE || _phase.value == SyncPhase.DONE) {
            runFakeSync()
        }
    }

    private fun runFakeSync() {
        _phase.value = SyncPhase.DISCOVERING
        _showPeers.value = false
        _summary.value = null
        _peers.value = listOf(
            PeerInfo(peerNames[0], "Waiting..."),
            PeerInfo(peerNames[1], "Waiting...")
        )

        viewModelScope.launch {
            // 1.0s: show peers, peer1 connecting
            delay(1000)
            _showPeers.value = true
            updatePeer(0, "Connecting...")

            // 1.8s: peer2 connecting
            delay(800)
            updatePeer(1, "Connecting...")

            // 2.6s: syncing phase
            delay(800)
            _phase.value = SyncPhase.SYNCING
            updatePeer(0, "Syncing...", 0.0)
            updatePeer(1, "Syncing...", 0.0)

            // Peer 1 progress ticks
            val p1Ticks = listOf(0.12, 0.28, 0.44, 0.61, 0.75, 0.89, 1.0)
            val p2Ticks = listOf(0.09, 0.22, 0.38, 0.55, 0.70, 0.84, 1.0)

            for (i in p1Ticks.indices) {
                delay(300)
                updatePeer(0, if (p1Ticks[i] >= 1.0) "Complete \u2014 14 sent \u00B7 9 received" else "Syncing...", p1Ticks[i])
                if (i < p2Ticks.size) {
                    updatePeer(1, if (p2Ticks[i] >= 1.0) "Complete \u2014 14 sent \u00B7 6 received" else "Syncing...", p2Ticks[i])
                }
            }

            // Final completion
            delay(300)
            updatePeer(1, "Complete \u2014 14 sent \u00B7 6 received", 1.0)

            delay(300)
            _phase.value = SyncPhase.DONE
            _summary.value = "Sync complete. 3 rangers up to date.\n28 records \u00B7 0 conflicts"
        }
    }

    private fun updatePeer(index: Int, status: String, progress: Double? = null) {
        val current = _peers.value.toMutableList()
        if (index < current.size) {
            current[index] = current[index].copy(
                status = status,
                progress = progress ?: current[index].progress
            )
            _peers.value = current
        }
    }
}
