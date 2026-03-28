package org.yac.llamarangers.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.local.entity.RangerProfileEntity
import org.yac.llamarangers.data.repository.RangerRepository
import org.yac.llamarangers.domain.model.enums.RangerRole
import org.yac.llamarangers.domain.model.enums.SyncStatus
import org.yac.llamarangers.service.auth.AuthManager
import java.util.UUID
import javax.inject.Inject

/**
 * Ports iOS LoginViewModel.
 * Manages ranger selection, PIN entry, and demo ranger seeding.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val rangerRepository: RangerRepository
) : ViewModel() {

    private val _rangers = MutableStateFlow<List<RangerProfileEntity>>(emptyList())
    val rangers: StateFlow<List<RangerProfileEntity>> = _rangers.asStateFlow()

    private val _selectedRanger = MutableStateFlow<RangerProfileEntity?>(null)
    val selectedRanger: StateFlow<RangerProfileEntity?> = _selectedRanger.asStateFlow()

    private val _enteredPIN = MutableStateFlow("")
    val enteredPIN: StateFlow<String> = _enteredPIN.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    init {
        loadRangers()
    }

    private fun loadRangers() {
        viewModelScope.launch {
            val all = rangerRepository.fetchAllRangers()
            _rangers.value = all
            if (all.isEmpty()) {
                seedDemoRangers()
            }
        }
    }

    fun selectRanger(ranger: RangerProfileEntity) {
        _selectedRanger.value = ranger
        _enteredPIN.value = ""
        _loginError.value = null
    }

    fun appendPINDigit(digit: String) {
        if (_enteredPIN.value.length >= 4) return
        _enteredPIN.value += digit
        if (_enteredPIN.value.length == 4) {
            attemptLogin()
        }
    }

    fun deletePINDigit() {
        val current = _enteredPIN.value
        if (current.isNotEmpty()) {
            _enteredPIN.value = current.dropLast(1)
        }
    }

    private fun attemptLogin() {
        val ranger = _selectedRanger.value
        if (ranger == null) {
            _loginError.value = "Please select a ranger first"
            _enteredPIN.value = ""
            return
        }
        val rangerId = try {
            UUID.fromString(ranger.id)
        } catch (_: Exception) {
            _loginError.value = "Invalid ranger ID"
            _enteredPIN.value = ""
            return
        }

        if (authManager.loginWithPIN(rangerId, _enteredPIN.value)) {
            // Auth state is updated via AuthManager.isAuthenticated StateFlow
        } else {
            _loginError.value = "Incorrect PIN"
            _enteredPIN.value = ""
        }
    }

    /**
     * Seeds 3 demo rangers on first launch, matching iOS behaviour.
     * Demo PIN: 1234
     */
    private fun seedDemoRangers() {
        viewModelScope.launch {
            val demoRangers = listOf(
                Triple("Alice Johnson", RangerRole.SENIOR_RANGER, UUID.randomUUID()),
                Triple("Bob Smith", RangerRole.RANGER, UUID.randomUUID()),
                Triple("Carol White", RangerRole.RANGER, UUID.randomUUID())
            )
            for ((name, role, _) in demoRangers) {
                rangerRepository.createRanger(
                    displayName = name,
                    role = role,
                    supabaseUid = UUID.randomUUID().toString()
                )
            }
            // Set the shared PIN
            authManager.setPIN("1234", UUID.randomUUID())
            // Reload
            _rangers.value = rangerRepository.fetchAllRangers()
        }
    }
}
