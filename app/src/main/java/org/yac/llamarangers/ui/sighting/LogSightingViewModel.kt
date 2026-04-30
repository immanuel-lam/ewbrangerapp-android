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
import org.yac.llamarangers.data.repository.SightingRepository
import org.yac.llamarangers.domain.model.enums.InfestationSize
import org.yac.llamarangers.domain.model.enums.InvasiveSpecies
import org.yac.llamarangers.service.auth.AuthManager
import org.yac.llamarangers.service.location.LocationManager
import javax.inject.Inject

enum class BiocontrolObservation(val displayName: String) {
    NOT_CHECKED("Not checked"),
    OBSERVED("Observed"),
    NOT_OBSERVED("Not seen"),
    UNSURE("Unsure")
}

@HiltViewModel
class LogSightingViewModel @Inject constructor(
    private val locationManager: LocationManager,
    private val sightingRepository: SightingRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _capturedLocation = MutableStateFlow<Location?>(null)
    val capturedLocation: StateFlow<Location?> = _capturedLocation.asStateFlow()

    private val _accuracyLevel = MutableStateFlow(LocationManager.AccuracyLevel.UNKNOWN)
    val accuracyLevel: StateFlow<LocationManager.AccuracyLevel> = _accuracyLevel.asStateFlow()

    private val _selectedSpecies = MutableStateFlow<InvasiveSpecies?>(null)
    val selectedSpecies: StateFlow<InvasiveSpecies?> = _selectedSpecies.asStateFlow()

    private val _selectedSize = MutableStateFlow(InfestationSize.SMALL)
    val selectedSize: StateFlow<InfestationSize> = _selectedSize.asStateFlow()

    private val _biocontrolObservation = MutableStateFlow(BiocontrolObservation.NOT_CHECKED)
    val biocontrolObservation: StateFlow<BiocontrolObservation> = _biocontrolObservation.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _photoFilenames = MutableStateFlow<List<String>>(emptyList())
    val photoFilenames: StateFlow<List<String>> = _photoFilenames.asStateFlow()

    private val _voiceNotePath = MutableStateFlow<String?>(null)
    val voiceNotePath: StateFlow<String?> = _voiceNotePath.asStateFlow()

    private val _areaEstimate = MutableStateFlow<String?>(null)
    val areaEstimate: StateFlow<String?> = _areaEstimate.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    private val _didSave = MutableStateFlow(false)
    val didSave: StateFlow<Boolean> = _didSave.asStateFlow()

    val canSave: StateFlow<Boolean> = combine(_capturedLocation, _selectedSpecies) { loc, species ->
        loc != null && species != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val controlRecommendation: String?
        get() {
            val species = _selectedSpecies.value ?: return null
            val methods = species.controlMethods.joinToString(" or ") { it.displayName }
            return "Recommended: $methods"
        }

    init {
        captureLocation()
    }

    fun setSelectedSpecies(species: InvasiveSpecies?) {
        _selectedSpecies.value = species
        if (species != InvasiveSpecies.LANTANA) {
            _biocontrolObservation.value = BiocontrolObservation.NOT_CHECKED
        }
    }

    fun setSelectedSize(size: InfestationSize) {
        _selectedSize.value = size
    }

    fun setBiocontrolObservation(observation: BiocontrolObservation) {
        _biocontrolObservation.value = observation
    }

    fun setNotes(text: String) {
        _notes.value = text
    }

    fun addPhoto(filename: String) {
        _photoFilenames.value = _photoFilenames.value + filename
    }

    fun setVoiceNotePath(path: String?) {
        _voiceNotePath.value = path
    }

    fun setAreaEstimate(estimate: String?) {
        _areaEstimate.value = estimate
    }

    private fun captureLocation() {
        viewModelScope.launch {
            val location = locationManager.captureLocation()
            _capturedLocation.value = location
            _accuracyLevel.value = locationManager.accuracyLevel.value
        }
    }

    fun recaptureLocation() {
        _capturedLocation.value = null
        _accuracyLevel.value = LocationManager.AccuracyLevel.UNKNOWN
        captureLocation()
    }

    fun save() {
        if (!canSave.value) return
        val location = _capturedLocation.value ?: return
        val species = _selectedSpecies.value ?: return
        val rangerId = authManager.currentRangerId.value?.toString()
        if (rangerId == null) {
            _saveError.value = "Not authenticated. Please log in again."
            return
        }

        _isSaving.value = true
        _saveError.value = null

        viewModelScope.launch {
            try {
                var finalNotes = _notes.value
                if (species == InvasiveSpecies.LANTANA && _biocontrolObservation.value != BiocontrolObservation.NOT_CHECKED) {
                    val bioNote = "[Lantana bug: ${_biocontrolObservation.value.displayName}]"
                    finalNotes = if (finalNotes.isEmpty()) bioNote else "$finalNotes $bioNote"
                    if (_biocontrolObservation.value == BiocontrolObservation.OBSERVED) {
                        finalNotes += " ⚠️ Biocontrol present - consider delaying herbicide"
                    }
                }

                sightingRepository.createSighting(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    horizontalAccuracy = location.accuracy.toDouble(),
                    variant = species,
                    infestationSize = _selectedSize.value,
                    notes = finalNotes.ifBlank { null },
                    photoFilenames = _photoFilenames.value,
                    rangerId = rangerId,
                    deviceId = "android",
                    infestationAreaEstimate = _areaEstimate.value,
                    voiceNotePath = _voiceNotePath.value
                )
                _didSave.value = true
            } catch (e: Exception) {
                _saveError.value = e.localizedMessage ?: "Failed to save"
            }
            _isSaving.value = false
        }
    }
}
