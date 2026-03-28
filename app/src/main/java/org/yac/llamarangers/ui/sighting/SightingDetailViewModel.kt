package org.yac.llamarangers.ui.sighting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.local.dao.SightingLogDao
import org.yac.llamarangers.data.local.entity.InfestationZoneEntity
import org.yac.llamarangers.data.local.entity.SightingLogEntity
import org.yac.llamarangers.data.local.entity.TreatmentRecordEntity
import org.yac.llamarangers.data.repository.TreatmentRepository
import org.yac.llamarangers.data.repository.ZoneRepository
import org.yac.llamarangers.domain.model.enums.InfestationSize
import org.yac.llamarangers.domain.model.enums.LantanaVariant
import org.yac.llamarangers.domain.model.enums.SyncStatus
import org.yac.llamarangers.ui.navigation.Screen
import javax.inject.Inject

/**
 * ViewModel for the sighting detail screen.
 * Ports iOS SightingDetailViewModel.
 */
@HiltViewModel
class SightingDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sightingDao: SightingLogDao,
    private val treatmentRepository: TreatmentRepository,
    private val zoneRepository: ZoneRepository
) : ViewModel() {

    private val sightingId: String = savedStateHandle[Screen.SightingDetail.ARG_SIGHTING_ID] ?: ""

    private val _sighting = MutableStateFlow<SightingLogEntity?>(null)
    val sighting: StateFlow<SightingLogEntity?> = _sighting.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _treatments = MutableStateFlow<List<TreatmentRecordEntity>>(emptyList())
    val treatments: StateFlow<List<TreatmentRecordEntity>> = _treatments.asStateFlow()

    private val _allZones = MutableStateFlow<List<InfestationZoneEntity>>(emptyList())
    val allZones: StateFlow<List<InfestationZoneEntity>> = _allZones.asStateFlow()

    init {
        loadSighting()
        loadTreatments()
        loadZones()
    }

    private fun loadSighting() {
        viewModelScope.launch {
            _sighting.value = sightingDao.findById(sightingId)
            _isLoading.value = false
        }
    }

    fun loadTreatments() {
        viewModelScope.launch {
            _treatments.value = treatmentRepository.fetchTreatmentsForSighting(sightingId)
        }
    }

    fun loadZones() {
        viewModelScope.launch {
            _allZones.value = zoneRepository.fetchAllZones()
        }
    }

    fun assignToZone(zoneId: String?) {
        viewModelScope.launch {
            zoneRepository.assignSighting(sightingId, zoneId)
            loadSighting()
            loadZones()
        }
    }

    val variant: LantanaVariant
        get() = _sighting.value?.let { LantanaVariant.fromValue(it.variant) } ?: LantanaVariant.UNKNOWN

    val size: InfestationSize
        get() = _sighting.value?.let { InfestationSize.fromValue(it.infestationSize) } ?: InfestationSize.SMALL

    val syncStatus: SyncStatus
        get() = _sighting.value?.let { SyncStatus.fromValue(it.syncStatus) } ?: SyncStatus.PENDING_CREATE

    val photoFilenames: List<String>
        get() = _sighting.value?.photoFilenames ?: emptyList()

    val assignedZone: InfestationZoneEntity?
        get() {
            val zoneId = _sighting.value?.infestationZoneId ?: return null
            return _allZones.value.firstOrNull { it.id == zoneId }
        }
}
