package org.yac.llamarangers.ui.safety

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.local.dao.SafetyCheckInDao
import org.yac.llamarangers.data.local.entity.SafetyCheckInEntity
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SafetyCheckInViewModel @Inject constructor(
    private val safetyDao: SafetyCheckInDao
) : ViewModel() {

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _intervalMinutes = MutableStateFlow(60)
    val intervalMinutes: StateFlow<Int> = _intervalMinutes.asStateFlow()

    private val _isSOSTriggered = MutableStateFlow(false)
    val isSOSTriggered: StateFlow<Boolean> = _isSOSTriggered.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val active = safetyDao.fetchAll().firstOrNull { it.isActive }
            if (active != null) {
                _isActive.value = true
                _intervalMinutes.value = active.intervalMinutes
                val elapsed = (System.currentTimeMillis() - (active.lastCheckInTime ?: active.startTime ?: System.currentTimeMillis())) / 1000
                val remaining = (active.intervalMinutes * 60) - elapsed.toInt()
                _remainingSeconds.value = if (remaining > 0) remaining else 0
                startTimer()
            }
        }
    }

    fun startSafetySession(minutes: Int) {
        _intervalMinutes.value = minutes
        _remainingSeconds.value = minutes * 60
        _isActive.value = true
        
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity = SafetyCheckInEntity(
            id = id,
            startTime = now,
            intervalMinutes = minutes,
            lastCheckInTime = now,
            isActive = true,
            notes = null
        )
        
        viewModelScope.launch {
            safetyDao.deleteAll()
            safetyDao.upsert(entity)
            startTimer()
        }
    }

    fun checkIn() {
        _remainingSeconds.value = _intervalMinutes.value * 60
        viewModelScope.launch {
            val active = safetyDao.fetchAll().firstOrNull { it.isActive }
            if (active != null) {
                safetyDao.upsert(active.copy(lastCheckInTime = System.currentTimeMillis()))
            }
        }
    }

    fun stopSession() {
        _isActive.value = false
        timerJob?.cancel()
        viewModelScope.launch {
            val active = safetyDao.fetchAll().firstOrNull { it.isActive }
            if (active != null) {
                safetyDao.upsert(active.copy(isActive = false))
            }
        }
    }

    fun triggerSOS() {
        _isSOSTriggered.value = true
    }

    fun dismissSOS() {
        _isSOSTriggered.value = false
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isActive.value) {
                delay(1000)
                if (_remainingSeconds.value > 0) {
                    _remainingSeconds.value -= 1
                } else {
                    triggerSOS()
                    _isActive.value = false
                }
            }
        }
    }

    val timeString: String
        get() {
            val h = _remainingSeconds.value / 3600
            val m = (_remainingSeconds.value % 3600) / 60
            val s = _remainingSeconds.value % 60
            return if (h > 0) {
                String.format("%02d:%02d:%02d", h, m, s)
            } else {
                String.format("%02d:%02d", m, s)
            }
        }
}
