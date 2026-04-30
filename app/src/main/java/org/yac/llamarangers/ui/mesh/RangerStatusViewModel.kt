package org.yac.llamarangers.ui.mesh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RangerStatus(
    val id: String,
    val name: String,
    val status: String,
    val lastSeen: Long,
    val zone: String? = null,
    val battery: Float? = null
)

@HiltViewModel
class RangerStatusViewModel @Inject constructor() : ViewModel() {

    private val _myStatus = MutableStateFlow("On Patrol")
    val myStatus: StateFlow<String> = _myStatus.asStateFlow()

    private val _nearbyRangers = MutableStateFlow<List<RangerStatus>>(emptyList())
    val nearbyRangers: StateFlow<List<RangerStatus>> = _nearbyRangers.asStateFlow()

    init {
        // Demo: simulate nearby rangers
        _nearbyRangers.value = listOf(
            RangerStatus("1", "Bob Smith", "On Patrol", System.currentTimeMillis() - 45000, "Creek Line East", 0.82f),
            RangerStatus("2", "Carol White", "Resting", System.currentTimeMillis() - 180000, "Base Camp", 0.45f)
        )
        
        // Update "last seen" timer for demo
        viewModelScope.launch {
            while(true) {
                delay(30000)
                // refresh or update logic
            }
        }
    }

    fun setMyStatus(status: String) {
        _myStatus.value = status
    }
}
