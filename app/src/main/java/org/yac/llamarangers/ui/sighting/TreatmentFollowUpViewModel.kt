package org.yac.llamarangers.ui.sighting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.local.dao.TreatmentFollowUpDao
import org.yac.llamarangers.data.local.entity.TreatmentFollowUpEntity
import org.yac.llamarangers.domain.model.enums.SyncStatus
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TreatmentFollowUpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val followUpDao: TreatmentFollowUpDao
) : ViewModel() {

    private val treatmentId: String = savedStateHandle["treatmentId"] ?: ""

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _didSave = MutableStateFlow(false)
    val didSave: StateFlow<Boolean> = _didSave.asStateFlow()

    fun saveFollowUp(
        percentDead: Double,
        regrowthLevel: String,
        notes: String?
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val id = UUID.randomUUID().toString()
            val entity = TreatmentFollowUpEntity(
                id = id,
                followUpDate = System.currentTimeMillis(),
                percentDead = percentDead,
                regrowthLevel = regrowthLevel,
                notes = notes,
                photoPath = null,
                syncStatus = SyncStatus.SYNCED.value, // Demo: auto-synced
                treatmentId = treatmentId
            )
            followUpDao.upsert(entity)
            _didSave.value = true
            _isSaving.value = false
        }
    }
}
