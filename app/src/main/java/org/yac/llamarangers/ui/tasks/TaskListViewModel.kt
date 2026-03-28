package org.yac.llamarangers.ui.tasks

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
import org.yac.llamarangers.data.local.entity.RangerTaskEntity
import org.yac.llamarangers.data.repository.TaskRepository
import org.yac.llamarangers.domain.model.enums.TaskPriority
import org.yac.llamarangers.service.auth.AuthManager
import javax.inject.Inject

/**
 * ViewModel for the task list screen.
 * Ports iOS TaskListViewModel.
 */
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<RangerTaskEntity>>(emptyList())

    private val _showCompleted = MutableStateFlow(false)
    val showCompleted: StateFlow<Boolean> = _showCompleted.asStateFlow()

    private val _filterPriority = MutableStateFlow<TaskPriority?>(null)
    val filterPriority: StateFlow<TaskPriority?> = _filterPriority.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val rangerId = authManager.currentRangerId.value?.toString()
            _tasks.value = taskRepository.fetchAllTasks(rangerId)
        }
    }

    fun setShowCompleted(show: Boolean) {
        _showCompleted.value = show
    }

    fun setFilterPriority(priority: TaskPriority?) {
        _filterPriority.value = priority
    }

    val displayedTasks: StateFlow<List<RangerTaskEntity>> = combine(
        _tasks, _showCompleted, _filterPriority
    ) { tasks, showComp, priority ->
        tasks
            .filter { task ->
                if (!showComp && task.isComplete) return@filter false
                if (priority != null && task.priority != priority.value) return@filter false
                true
            }
            .sortedWith(compareBy<RangerTaskEntity> { it.isComplete }
                .thenBy { TaskPriority.fromValue(it.priority).sortOrder }
                .thenBy { it.dueDate ?: Long.MAX_VALUE }
                .thenByDescending { it.createdAt }
            )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val overdueCountFlow: StateFlow<Int> = _tasks.combine(_showCompleted) { tasks, _ ->
        val now = System.currentTimeMillis()
        tasks.count { !it.isComplete && (it.dueDate ?: Long.MAX_VALUE) < now }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun toggle(task: RangerTaskEntity) {
        viewModelScope.launch {
            taskRepository.toggleComplete(task.id)
            load()
        }
    }

    fun delete(task: RangerTaskEntity) {
        viewModelScope.launch {
            taskRepository.deleteTask(task.id)
            load()
        }
    }
}
