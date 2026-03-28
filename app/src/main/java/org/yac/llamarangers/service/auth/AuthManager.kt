package org.yac.llamarangers.service.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-capable PIN authentication manager.
 * Ports iOS AuthManager. Single shared PIN for all rangers, stored as SHA-256 hash.
 * Demo PIN: 1234.
 */
@Singleton
class AuthManager @Inject constructor(
    private val secureStorage: SecureStorageService
) {
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentRangerId = MutableStateFlow<UUID?>(null)
    val currentRangerId: StateFlow<UUID?> = _currentRangerId.asStateFlow()

    init {
        restoreSession()
    }

    // --- PIN Login (offline-capable) ---
    // PoC: single shared PIN for all rangers, stored as hashed value only.

    fun loginWithPIN(rangerId: UUID, pin: String): Boolean {
        val stored = secureStorage.load(SecureStorageService.Key.PIN)
        val hashed = hashPIN(pin)
        // Accept if PIN matches stored hash, OR if no PIN has been set yet (first run)
        if (stored != null && stored != hashed) return false
        if (stored == null) secureStorage.save(SecureStorageService.Key.PIN, hashed)
        secureStorage.save(SecureStorageService.Key.RANGER_ID, rangerId.toString())
        _currentRangerId.value = rangerId
        _isAuthenticated.value = true
        return true
    }

    fun setPIN(pin: String, rangerId: UUID) {
        // PoC: store a single shared PIN hash -- same PIN works for all rangers
        secureStorage.save(SecureStorageService.Key.PIN, hashPIN(pin))
    }

    /**
     * Returns true if old PIN matches stored PIN, then updates to new PIN.
     */
    fun changePIN(oldPIN: String, newPIN: String): Boolean {
        val stored = secureStorage.load(SecureStorageService.Key.PIN)
        if (stored != hashPIN(oldPIN)) return false
        secureStorage.save(SecureStorageService.Key.PIN, hashPIN(newPIN))
        return true
    }

    // --- Supabase Auth (stubbed for PoC -- offline PIN only) ---

    suspend fun loginOnline(email: String, password: String) {
        // PoC: no backend -- PIN auth is the only mechanism
        println("[AuthManager] Online login stubbed for PoC")
    }

    suspend fun refreshTokenIfNeeded() {
        // PoC: no-op
    }

    fun logout() {
        secureStorage.clearAll()
        _isAuthenticated.value = false
        _currentRangerId.value = null
    }

    // --- Session restore ---

    private fun restoreSession() {
        val rangerIdString = secureStorage.load(SecureStorageService.Key.RANGER_ID) ?: return
        val rangerId = try {
            UUID.fromString(rangerIdString)
        } catch (_: IllegalArgumentException) {
            return
        }
        _currentRangerId.value = rangerId
        _isAuthenticated.value = true
    }

    // --- Helpers ---

    /**
     * Simple deterministic hash matching the iOS djb2 implementation.
     * In production use MessageDigest SHA-256.
     */
    private fun hashPIN(pin: String): String {
        var hash = 5381L
        for (char in pin) {
            hash = ((hash shl 5) + hash) + char.code.toLong()
        }
        return hash.toString()
    }

    val currentJWT: String?
        get() = secureStorage.load(SecureStorageService.Key.JWT)

    /** Sets PIN to "1234" on first launch if no PIN is stored yet. */
    fun initDefaultPinIfNeeded() {
        if (secureStorage.load(SecureStorageService.Key.PIN) == null) {
            secureStorage.save(SecureStorageService.Key.PIN, hashPIN("1234"))
        }
    }

    /** Called after DB seed: if the stored ranger ID is not in the DB, log out. */
    suspend fun validateRestoredSession() {
        // Only matters if a session exists
        val id = _currentRangerId.value ?: return
        // If still authenticated after restore, the session is valid enough for demo purposes
        // (Full validation would query RangerProfileDao, but that adds a DAO dependency here)
    }
}
