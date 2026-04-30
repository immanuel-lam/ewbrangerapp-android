package org.yac.llamarangers.ui.map

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.local.entity.InfestationZoneEntity
import org.yac.llamarangers.data.local.entity.InfestationZoneSnapshotEntity
import org.yac.llamarangers.data.local.entity.PatrolRecordEntity
import org.yac.llamarangers.data.local.entity.SightingLogEntity
import org.yac.llamarangers.data.repository.PatrolRepository
import org.yac.llamarangers.data.repository.SightingRepository
import org.yac.llamarangers.data.repository.ZoneRepository
import org.yac.llamarangers.resources.PortStewartZones
import org.yac.llamarangers.service.auth.AuthManager
import javax.inject.Inject

/**
 * Ports iOS MapViewModel + draw-mode state from MapContainerView.
 * Manages sightings, zones (with snapshots), patrols, map layers, timeline, and draw mode.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val sightingRepository: SightingRepository,
    private val zoneRepository: ZoneRepository,
    private val patrolRepository: PatrolRepository,
    private val authManager: AuthManager
) : ViewModel() {

    enum class MapType { SATELLITE, HYBRID, STANDARD }

    // --- Data ---
    private val _sightings = MutableStateFlow<List<SightingLogEntity>>(emptyList())
    val sightings: StateFlow<List<SightingLogEntity>> = _sightings.asStateFlow()

    private val _zones = MutableStateFlow<List<InfestationZoneEntity>>(emptyList())
    val zones: StateFlow<List<InfestationZoneEntity>> = _zones.asStateFlow()

    private val _patrols = MutableStateFlow<List<PatrolRecordEntity>>(emptyList())
    val patrols: StateFlow<List<PatrolRecordEntity>> = _patrols.asStateFlow()

    /** Zone ID -> latest snapshot (if any). Loaded alongside zones. */
    private val _zoneSnapshots = MutableStateFlow<Map<String, InfestationZoneSnapshotEntity>>(emptyMap())
    val zoneSnapshots: StateFlow<Map<String, InfestationZoneSnapshotEntity>> = _zoneSnapshots.asStateFlow()

    /** Zone ID -> list of sighting coords for that zone (used for circle fallback). */
    private val _zoneSightings = MutableStateFlow<Map<String, List<Pair<Double, Double>>>>(emptyMap())
    val zoneSightings: StateFlow<Map<String, List<Pair<Double, Double>>>> = _zoneSightings.asStateFlow()

    /** Zone ID -> number of sightings linked to that zone. */
    private val _sightingCountsByZone = MutableStateFlow<Map<String, Int>>(emptyMap())
    val sightingCountsByZone: StateFlow<Map<String, Int>> = _sightingCountsByZone.asStateFlow()

    // --- Map layer toggles ---
    private val _mapType = MutableStateFlow(MapType.SATELLITE)
    val mapType: StateFlow<MapType> = _mapType.asStateFlow()

    private val _showSightings = MutableStateFlow(true)
    val showSightings: StateFlow<Boolean> = _showSightings.asStateFlow()

    private val _showZones = MutableStateFlow(true)
    val showZones: StateFlow<Boolean> = _showZones.asStateFlow()

    private val _showPatrols = MutableStateFlow(true)
    val showPatrols: StateFlow<Boolean> = _showPatrols.asStateFlow()

    // --- Timeline ---
    private val _timelineDate = MutableStateFlow(System.currentTimeMillis())
    val timelineDate: StateFlow<Long> = _timelineDate.asStateFlow()

    private val _isPlayingTimeline = MutableStateFlow(false)
    val isPlayingTimeline: StateFlow<Boolean> = _isPlayingTimeline.asStateFlow()

    private var timelineHandler: Handler? = null
    private var timelineRunnable: Runnable? = null

    // --- Draw mode ---
    private val _isDrawing = MutableStateFlow(false)
    val isDrawing: StateFlow<Boolean> = _isDrawing.asStateFlow()

    private val _drawingZone = MutableStateFlow<InfestationZoneEntity?>(null)
    val drawingZone: StateFlow<InfestationZoneEntity?> = _drawingZone.asStateFlow()

    private val _drawVertices = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val drawVertices: StateFlow<List<Pair<Double, Double>>> = _drawVertices.asStateFlow()

    // --- Zone picker ---
    private val _showZonePicker = MutableStateFlow(false)
    val showZonePicker: StateFlow<Boolean> = _showZonePicker.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            try {
                _sightings.value = sightingRepository.fetchAllSightings()
                _zones.value = zoneRepository.fetchAllZones()
                _patrols.value = patrolRepository.fetchAllPatrols()

                // Load latest snapshot per zone and sightings per zone for circle fallback
                val snapshots = mutableMapOf<String, InfestationZoneSnapshotEntity>()
                val sightingsMap = mutableMapOf<String, List<Pair<Double, Double>>>()

                for (zone in _zones.value) {
                    val zoneSnapshots = zoneRepository.fetchSnapshotsForZone(zone.id)
                    // fetchSnapshotsForZone returns ordered by snapshot_date DESC, so first is latest
                    zoneSnapshots.firstOrNull()?.let { snapshots[zone.id] = it }

                    // Load sightings for circle fallback (only if no snapshot)
                    if (zoneSnapshots.isEmpty()) {
                        val zoneSightings = zoneRepository.fetchSightingsForZone(zone.id)
                        if (zoneSightings.isNotEmpty()) {
                            sightingsMap[zone.id] = zoneSightings.map { it.latitude to it.longitude }
                        }
                    }
                }

                _zoneSnapshots.value = snapshots
                _zoneSightings.value = sightingsMap
                _sightingCountsByZone.value = _sightings.value
                    .groupBy { it.infestationZoneId ?: "" }
                    .filterKeys { it.isNotEmpty() }
                    .mapValues { (_, v) -> v.size }

                if (!_isPlayingTimeline.value) {
                    _timelineDate.value = System.currentTimeMillis()
                }
            } catch (_: Exception) {
                // Data remains at last-known state; map stays usable
            }
        }
    }

    // --- Map type ---
    fun setMapType(type: MapType) { _mapType.value = type }

    // --- Layer toggles ---
    fun toggleSightings() { _showSightings.value = !_showSightings.value }
    fun toggleZones() { _showZones.value = !_showZones.value }
    fun togglePatrols() { _showPatrols.value = !_showPatrols.value }

    // --- Timeline ---
    fun setTimelineDate(epochMillis: Long) { _timelineDate.value = epochMillis }

    val filteredSightings: List<SightingLogEntity>
        get() {
            if (!_showSightings.value) return emptyList()
            return _sightings.value.filter { it.createdAt <= _timelineDate.value }
        }

    data class PatrolAnnotationData(
        val patrol: PatrolRecordEntity,
        val latitude: Double,
        val longitude: Double
    )

    val filteredPatrols: List<PatrolAnnotationData>
        get() {
            if (!_showPatrols.value) return emptyList()
            return _patrols.value.mapNotNull { patrol ->
                val area = patrol.areaName ?: return@mapNotNull null
                val coord = PortStewartZones.areaCoordinates[area] ?: return@mapNotNull null
                PatrolAnnotationData(patrol, coord.latitude, coord.longitude)
            }
        }

    val dateRange: Pair<Long, Long>
        get() {
            val earliest = _sightings.value.minOfOrNull { it.createdAt }
                ?: System.currentTimeMillis()
            return earliest to System.currentTimeMillis()
        }

    fun toggleTimeline() {
        if (_isPlayingTimeline.value) {
            stopTimeline()
        } else {
            startTimeline()
        }
    }

    private fun startTimeline() {
        _isPlayingTimeline.value = true
        val earliest = _sightings.value.minOfOrNull { it.createdAt }
            ?: System.currentTimeMillis()
        _timelineDate.value = earliest

        val handler = Handler(Looper.getMainLooper())
        timelineHandler = handler

        val runnable = object : Runnable {
            override fun run() {
                val current = _timelineDate.value
                val next = current + 30L * 24 * 60 * 60 * 1000
                if (next > System.currentTimeMillis()) {
                    stopTimeline()
                } else {
                    _timelineDate.value = next
                    handler.postDelayed(this, 100)
                }
            }
        }
        timelineRunnable = runnable
        handler.postDelayed(runnable, 100)
    }

    private fun stopTimeline() {
        _isPlayingTimeline.value = false
        timelineRunnable?.let { timelineHandler?.removeCallbacks(it) }
        timelineHandler = null
        timelineRunnable = null
    }

    // --- Draw mode ---
    fun requestDrawMode() {
        _showZonePicker.value = true
    }

    fun dismissZonePicker() {
        _showZonePicker.value = false
    }

    fun enterDrawMode(zone: InfestationZoneEntity) {
        _showZonePicker.value = false
        _drawingZone.value = zone
        _drawVertices.value = emptyList()
        _isDrawing.value = true
    }

    fun addVertex(lat: Double, lon: Double) {
        _drawVertices.value = _drawVertices.value + (lat to lon)
    }

    fun undoVertex() {
        val current = _drawVertices.value
        if (current.isNotEmpty()) {
            _drawVertices.value = current.dropLast(1)
        }
    }

    fun cancelDraw() {
        _isDrawing.value = false
        _drawingZone.value = null
        _drawVertices.value = emptyList()
    }

    fun savePolygon() {
        val zone = _drawingZone.value ?: return
        val vertices = _drawVertices.value
        if (vertices.size < 3) return
        val rangerId = authManager.currentRangerId.value?.toString() ?: return

        val coordinates = vertices.map { listOf(it.first, it.second) }

        viewModelScope.launch {
            try {
                zoneRepository.addSnapshot(
                    zoneId = zone.id,
                    coordinates = coordinates,
                    area = 0.0,
                    createdByRangerId = rangerId
                )
                // Clear draw mode only on success
                _isDrawing.value = false
                _drawingZone.value = null
                _drawVertices.value = emptyList()
                // Reload data
                load()
            } catch (_: Exception) {
                // Keep draw mode open so the user can retry;
                // vertices are preserved
            }
        }
    }

    // --- Delete actions ---
    fun deleteSighting(sightingId: String) {
        viewModelScope.launch {
            try {
                sightingRepository.deleteSighting(sightingId)
            } catch (_: Exception) { /* best-effort */ }
            load()
        }
    }

    fun deletePatrol(patrolId: String) {
        viewModelScope.launch {
            try {
                patrolRepository.deletePatrol(patrolId)
            } catch (_: Exception) { /* best-effort */ }
            load()
        }
    }

    fun deleteZone(zoneId: String) {
        viewModelScope.launch {
            try {
                zoneRepository.deleteZone(zoneId)
            } catch (_: Exception) { /* best-effort */ }
            load()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimeline()
    }
}
