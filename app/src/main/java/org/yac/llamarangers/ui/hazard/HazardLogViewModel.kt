package org.yac.llamarangers.ui.hazard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.local.dao.HazardLogDao
import org.yac.llamarangers.data.local.entity.HazardLogEntity
import org.yac.llamarangers.service.location.LocationManager
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HazardLogViewModel @Inject constructor(
    private val hazardDao: HazardLogDao,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _hazards = MutableStateFlow<List<HazardLogEntity>>(emptyList())
    val hazards: StateFlow<List<HazardLogEntity>> = _hazards.asStateFlow()

    init {
        loadHazards()
    }

    fun loadHazards() {
        viewModelScope.launch {
            _hazards.value = hazardDao.fetchAll()
        }
    }

    fun logHazard(
        title: String,
        type: String,
        severity: String,
        notes: String?,
        photoPath: String?
    ) {
        viewModelScope.launch {
            val location = locationManager.captureLocation()
            val entity = HazardLogEntity(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                title = title,
                hazardType = type,
                severity = severity,
                notes = notes,
                latitude = location?.latitude ?: 0.0,
                longitude = location?.longitude ?: 0.0,
                photoPath = photoPath,
                syncedToCloud = false
            )
            hazardDao.upsert(entity)
            loadHazards()
        }
    }

    fun deleteHazard(hazard: HazardLogEntity) {
        viewModelScope.launch {
            hazardDao.delete(hazard)
            loadHazards()
        }
    }
}
