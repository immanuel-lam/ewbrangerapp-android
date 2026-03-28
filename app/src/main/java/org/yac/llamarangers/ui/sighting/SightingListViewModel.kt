package org.yac.llamarangers.ui.sighting

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.local.entity.SightingLogEntity
import org.yac.llamarangers.data.repository.RangerRepository
import org.yac.llamarangers.data.repository.SightingRepository
import org.yac.llamarangers.domain.model.enums.LantanaVariant
import org.yac.llamarangers.service.location.LocationManager
import javax.inject.Inject

/**
 * Ports iOS SightingListViewModel.
 * List with search/filter, distance from current location,
 * and ranger name lookup for display in sighting rows.
 */
@HiltViewModel
class SightingListViewModel @Inject constructor(
    private val sightingRepository: SightingRepository,
    private val rangerRepository: RangerRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _sightings = MutableStateFlow<List<SightingLogEntity>>(emptyList())
    val sightings: StateFlow<List<SightingLogEntity>> = _sightings.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _filterVariant = MutableStateFlow<LantanaVariant?>(null)
    val filterVariant: StateFlow<LantanaVariant?> = _filterVariant.asStateFlow()

    /** Map of ranger ID -> display name, used by SightingRow to show who logged each sighting. */
    private val _rangerNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val rangerNames: StateFlow<Map<String, String>> = _rangerNames.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _sightings.value = sightingRepository.fetchAllSightings()
            // Load ranger names for display in sighting rows
            val rangers = rangerRepository.fetchAllRangers()
            _rangerNames.value = rangers.associate { it.id to (it.displayName ?: "Ranger") }
        }
    }

    fun setSearchText(text: String) {
        _searchText.value = text
    }

    fun setFilterVariant(variant: LantanaVariant?) {
        _filterVariant.value = variant
    }

    val filteredSightings: StateFlow<List<SightingLogEntity>> = combine(
        _sightings, _searchText, _filterVariant
    ) { sightings, query, variant ->
        var result = sightings
        if (query.isNotEmpty()) {
            result = result.filter {
                it.variant.contains(query, ignoreCase = true) ||
                        (it.notes?.contains(query, ignoreCase = true) == true)
            }
        }
        if (variant != null) {
            result = result.filter { it.variant == variant.value }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun delete(sightingId: String) {
        viewModelScope.launch {
            sightingRepository.deleteSighting(sightingId)
            load()
        }
    }

    fun formattedDistance(sighting: SightingLogEntity): String? {
        val current = locationManager.currentLocation.value ?: return null
        val target = Location("target").apply {
            latitude = sighting.latitude
            longitude = sighting.longitude
        }
        val d = current.distanceTo(target).toDouble()
        return if (d < 1000) "%.0fm".format(d) else "%.1fkm".format(d / 1000)
    }
}
