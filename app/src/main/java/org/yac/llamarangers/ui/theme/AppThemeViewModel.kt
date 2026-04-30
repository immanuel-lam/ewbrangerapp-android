package org.yac.llamarangers.ui.theme

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppThemeViewModel @Inject constructor() : ViewModel() {

    private val _isRedLightMode = MutableStateFlow(false)
    val isRedLightMode: StateFlow<Boolean> = _isRedLightMode.asStateFlow()

    fun setRedLightMode(enabled: Boolean) {
        _isRedLightMode.value = enabled
    }
    
    fun toggleRedLightMode() {
        _isRedLightMode.value = !_isRedLightMode.value
    }
}
