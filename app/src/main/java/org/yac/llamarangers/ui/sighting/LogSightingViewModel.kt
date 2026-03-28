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
import org.yac.llamarangers.domain.model.enums.LantanaVariant
import org.yac.llamarangers.service.auth.AuthManager
import org.yac.llamarangers.service.location.LocationManager
import javax.inject.Inject

/**
 * Ports iOS LogSightingViewModel.
 * GPS capture, variant/size selection, photo capture, and save.
 */
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

    private val _selectedVariant = MutableStateFlow<LantanaVariant?>(null)
    val selectedVariant: StateFlow<LantanaVariant?> = _selectedVariant.asStateFlow()

    private val _selectedSize = MutableStateFlow(InfestationSize.SMALL)
    val selectedSize: StateFlow<InfestationSize> = _selectedSize.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _photoFilenames = MutableStateFlow<List<String>>(emptyList())
    val photoFilenames: StateFlow<List<String>> = _photoFilenames.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    private val _didSave = MutableStateFlow(false)
    val didSave: StateFlow<Boolean> = _didSave.asStateFlow()

    val canSave: StateFlow<Boolean> = combine(_capturedLocation, _selectedVariant) { loc, variant ->
        loc != null && variant != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val controlRecommendation: String?
        get() {
            val variant = _selectedVariant.value ?: return null
            val methods = variant.controlMethods.joinToString(" or ") { it.displayName }
            return "Recommended: $methods"
        }

    init {
        captureLocation()
    }

    fun setSelectedVariant(variant: LantanaVariant?) {
        _selectedVariant.value = variant
    }

    fun setSelectedSize(size: InfestationSize) {
        _selectedSize.value = size
    }

    fun setNotes(text: String) {
        _notes.value = text
    }

    fun addPhoto(filename: String) {
        _photoFilenames.value = _photoFilenames.value + filename
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
        val variant = _selectedVariant.value ?: return
        val rangerId = authManager.currentRangerId.value?.toString() ?: return

        _isSaving.value = true
        _saveError.value = null

        viewModelScope.launch {
            try {
                sightingRepository.createSighting(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    horizontalAccuracy = location.accuracy.toDouble(),
                    variant = variant,
                    infestationSize = _selectedSize.value,
                    notes = _notes.value.ifBlank { null },
                    photoFilenames = _photoFilenames.value,
                    rangerId = rangerId,
                    deviceId = "android"
                )
                _didSave.value = true
            } catch (e: Exception) {
                _saveError.value = e.localizedMessage ?: "Failed to save"
            }
            _isSaving.value = false
        }
    }
}
