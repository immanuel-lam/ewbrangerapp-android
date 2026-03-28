package org.yac.llamarangers.ui.patrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.local.entity.PatrolRecordEntity
import org.yac.llamarangers.data.repository.PatrolRepository
import org.yac.llamarangers.domain.model.PatrolChecklistItem
import org.yac.llamarangers.resources.PortStewartZones
import org.yac.llamarangers.service.auth.AuthManager
import javax.inject.Inject

@HiltViewModel
class PatrolViewModel @Inject constructor(
    private val patrolRepository: PatrolRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val gson = Gson()

    private val _patrols = MutableStateFlow<List<PatrolRecordEntity>>(emptyList())
    val patrols: StateFlow<List<PatrolRecordEntity>> = _patrols.asStateFlow()

    private val _activePatrol = MutableStateFlow<PatrolRecordEntity?>(null)
    val activePatrol: StateFlow<PatrolRecordEntity?> = _activePatrol.asStateFlow()

    private val _activeChecklistItems = MutableStateFlow<List<PatrolChecklistItem>>(emptyList())
    val activeChecklistItems: StateFlow<List<PatrolChecklistItem>> = _activeChecklistItems.asStateFlow()

    private val _selectedAreaName = MutableStateFlow(PortStewartZones.patrolAreas[0])
    val selectedAreaName: StateFlow<String> = _selectedAreaName.asStateFlow()

    init {
        load()
    }

    fun setSelectedArea(area: String) {
        _selectedAreaName.value = area
    }

    fun load() {
        viewModelScope.launch {
            _patrols.value = patrolRepository.fetchAllPatrols()
            val rangerId = authManager.currentRangerId.value?.toString() ?: return@launch
            val active = patrolRepository.fetchActivePatrol(rangerId)
            _activePatrol.value = active
            if (active != null) {
                _activeChecklistItems.value = loadChecklist(active)
            }
        }
    }

    fun startPatrol() {
        viewModelScope.launch {
            if (_activePatrol.value != null) return@launch
            val rangerId = authManager.currentRangerId.value?.toString() ?: return@launch
            val area = _selectedAreaName.value
            val checklist = PortStewartZones.defaultChecklist(area)
            val checklistJson = gson.toJson(checklist)

            val patrol = patrolRepository.createPatrol(
                areaName = area,
                rangerId = rangerId,
                checklistItemsJson = checklistJson
            )
            _activePatrol.value = patrol
            _activeChecklistItems.value = checklist
            load()
        }
    }

    fun toggleItem(item: PatrolChecklistItem) {
        viewModelScope.launch {
            val patrol = _activePatrol.value ?: return@launch
            val items = _activeChecklistItems.value.toMutableList()
            val idx = items.indexOfFirst { it.id == item.id }
            if (idx >= 0) {
                val toggled = items[idx]
                items[idx] = toggled.copy(
                    isComplete = !toggled.isComplete,
                    completedAt = if (!toggled.isComplete) System.currentTimeMillis() else null
                )
            }
            _activeChecklistItems.value = items
            patrolRepository.updateChecklist(patrol.id, gson.toJson(items))
        }
    }

    fun finishPatrol() {
        viewModelScope.launch {
            val patrol = _activePatrol.value ?: return@launch
            patrolRepository.finishPatrol(patrol.id)
            _activePatrol.value = null
            _activeChecklistItems.value = emptyList()
            load()
        }
    }

    val completionPercentage: Float
        get() {
            val items = _activeChecklistItems.value
            if (items.isEmpty()) return 0f
            val completed = items.count { it.isComplete }
            return completed.toFloat() / items.size.toFloat()
        }

    private fun loadChecklist(patrol: PatrolRecordEntity): List<PatrolChecklistItem> {
        return try {
            val type = object : TypeToken<List<PatrolChecklistItem>>() {}.type
            gson.fromJson<List<PatrolChecklistItem>>(patrol.checklistItems, type) ?: emptyList()
        } catch (_: Exception) {
            PortStewartZones.defaultChecklist(patrol.areaName ?: "")
        }
    }
}
