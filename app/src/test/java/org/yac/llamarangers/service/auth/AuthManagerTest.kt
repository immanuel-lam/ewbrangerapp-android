package org.yac.llamarangers.service.auth

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.util.UUID

/**
 * Unit tests for AuthManager using a mocked SecureStorageService.
 */
class AuthManagerTest {

    private lateinit var storage: SecureStorageService
    private val store = mutableMapOf<String, String>()

    private fun buildAuthManager(): AuthManager = AuthManager(storage)

    @Before
    fun setUp() {
        store.clear()
        storage = mock {
            on { save(any(), any()) } doAnswer { inv ->
                store[(inv.arguments[0] as SecureStorageService.Key).value] = inv.arguments[1] as String
                true
            }
            on { load(any()) } doAnswer { inv ->
                store[(inv.arguments[0] as SecureStorageService.Key).value]
            }
            on { delete(any()) } doAnswer { inv ->
                store.remove((inv.arguments[0] as SecureStorageService.Key).value)
                true
            }
            on { clearAll() } doAnswer {
                store.clear()
                Unit
            }
        }
    }

    // ── Login ────────────────────────────────────────────────────────────────

    @Test
    fun `login with correct PIN succeeds`() {
        val authManager = buildAuthManager()
        val rangerId = UUID.randomUUID()
        assertTrue(authManager.loginWithPIN(rangerId, "1234"))
        assertTrue(authManager.isAuthenticated.value)
        assertEquals(rangerId, authManager.currentRangerId.value)
    }

    @Test
    fun `first login with any PIN succeeds and stores it`() {
        val authManager = buildAuthManager()
        assertTrue(authManager.loginWithPIN(UUID.randomUUID(), "9999"))
        assertTrue(authManager.isAuthenticated.value)
        assertNotNull(store[SecureStorageService.Key.PIN.value])
    }

    @Test
    fun `login with wrong PIN fails when PIN is already set`() {
        val authManager = buildAuthManager()
        authManager.loginWithPIN(UUID.randomUUID(), "1234")

        // Create a new AuthManager (simulates app restart) — PIN persists in storage
        // Note: session restores from storage, so isAuthenticated is true from init.
        // The failed login attempt should return false.
        val authManager2 = buildAuthManager()
        assertFalse(authManager2.loginWithPIN(UUID.randomUUID(), "5678"))
    }

    @Test
    fun `login with correct PIN succeeds on new AuthManager instance`() {
        val authManager = buildAuthManager()
        authManager.loginWithPIN(UUID.randomUUID(), "1234")

        // Simulate app restart — PIN persists
        val authManager2 = buildAuthManager()
        val rangerId2 = UUID.randomUUID()
        assertTrue(authManager2.loginWithPIN(rangerId2, "1234"))
        assertEquals(rangerId2, authManager2.currentRangerId.value)
    }

    // ── Logout ───────────────────────────────────────────────────────────────

    @Test
    fun `logout clears auth state`() {
        val authManager = buildAuthManager()
        authManager.loginWithPIN(UUID.randomUUID(), "1234")
        authManager.logout()
        assertFalse(authManager.isAuthenticated.value)
        assertNull(authManager.currentRangerId.value)
    }

    @Test
    fun `logout clears storage`() {
        val authManager = buildAuthManager()
        authManager.loginWithPIN(UUID.randomUUID(), "1234")
        authManager.logout()
        assertTrue(store.isEmpty())
    }

    // ── Session restore ──────────────────────────────────────────────────────

    @Test
    fun `session restores when valid ranger ID and PIN exist`() {
        val rangerId = UUID.randomUUID()
        store[SecureStorageService.Key.RANGER_ID.value] = rangerId.toString()
        store[SecureStorageService.Key.PIN.value] = "somehash"

        val restored = buildAuthManager()
        assertTrue(restored.isAuthenticated.value)
        assertEquals(rangerId, restored.currentRangerId.value)
    }

    @Test
    fun `session does not restore when no ranger ID stored`() {
        val restored = buildAuthManager()
        assertFalse(restored.isAuthenticated.value)
        assertNull(restored.currentRangerId.value)
    }

    @Test
    fun `session does not restore when ranger ID is corrupted`() {
        store[SecureStorageService.Key.RANGER_ID.value] = "not-a-uuid"
        store[SecureStorageService.Key.PIN.value] = "somehash"

        val restored = buildAuthManager()
        assertFalse(restored.isAuthenticated.value)
        assertNull(store[SecureStorageService.Key.RANGER_ID.value])
    }

    @Test
    fun `session does not restore when PIN is missing but ranger ID exists`() {
        store[SecureStorageService.Key.RANGER_ID.value] = UUID.randomUUID().toString()

        val restored = buildAuthManager()
        assertFalse(restored.isAuthenticated.value)
        assertNull(store[SecureStorageService.Key.RANGER_ID.value])
    }

    // ── Change PIN ───────────────────────────────────────────────────────────

    @Test
    fun `changePIN succeeds with correct old PIN`() {
        val authManager = buildAuthManager()
        authManager.loginWithPIN(UUID.randomUUID(), "1234")
        assertTrue(authManager.changePIN("1234", "5678"))

        // Verify new PIN works on fresh instance
        val authManager2 = buildAuthManager()
        assertTrue(authManager2.loginWithPIN(UUID.randomUUID(), "5678"))
    }

    @Test
    fun `changePIN fails with wrong old PIN`() {
        val authManager = buildAuthManager()
        authManager.loginWithPIN(UUID.randomUUID(), "1234")
        assertFalse(authManager.changePIN("wrong", "5678"))

        // Verify old PIN still works
        val authManager2 = buildAuthManager()
        assertTrue(authManager2.loginWithPIN(UUID.randomUUID(), "1234"))
    }

    // ── Default PIN ──────────────────────────────────────────────────────────

    @Test
    fun `initDefaultPinIfNeeded sets PIN when none exists`() {
        val authManager = buildAuthManager()
        assertNull(store[SecureStorageService.Key.PIN.value])
        authManager.initDefaultPinIfNeeded()
        assertNotNull(store[SecureStorageService.Key.PIN.value])
    }

    @Test
    fun `initDefaultPinIfNeeded does not overwrite existing PIN`() {
        val authManager = buildAuthManager()
        authManager.loginWithPIN(UUID.randomUUID(), "9999")
        val storedPin = store[SecureStorageService.Key.PIN.value]

        authManager.initDefaultPinIfNeeded()
        assertEquals(storedPin, store[SecureStorageService.Key.PIN.value])
    }

    @Test
    fun `default PIN 1234 works after initDefaultPinIfNeeded`() {
        val authManager = buildAuthManager()
        authManager.initDefaultPinIfNeeded()
        assertTrue(authManager.loginWithPIN(UUID.randomUUID(), "1234"))
    }
}
