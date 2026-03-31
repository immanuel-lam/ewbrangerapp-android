package org.yac.llamarangers.service.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure key-value store backed by EncryptedSharedPreferences (AndroidX Security).
 * Replaces iOS KeychainService.
 */
@Singleton
class SecureStorageService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SecureStorageService"
        private const val PREFS_FILE = "org.yac.llamarangers.secure_prefs"
    }

    enum class Key(val value: String) {
        JWT("org.yac.llamarangers.jwt"),
        REFRESH_TOKEN("org.yac.llamarangers.refresh"),
        PIN("org.yac.llamarangers.pin"),
        RANGER_ID("org.yac.llamarangers.rangerID")
    }

    private val prefs: SharedPreferences? by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Keystore corruption (e.g. after backup restore) -- wipe and retry once
            Log.e(TAG, "EncryptedSharedPreferences corrupted, clearing and retrying", e)
            try {
                context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
                    .edit().clear().commit()
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    context,
                    PREFS_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (retryEx: Exception) {
                Log.e(TAG, "EncryptedSharedPreferences retry failed, secure storage unavailable", retryEx)
                null
            }
        }
    }

    fun save(key: Key, value: String): Boolean {
        return prefs?.edit()?.putString(key.value, value)?.commit() ?: false
    }

    fun load(key: Key): String? {
        return try {
            prefs?.getString(key.value, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load key ${key.value}", e)
            null
        }
    }

    fun delete(key: Key): Boolean {
        return prefs?.edit()?.remove(key.value)?.commit() ?: false
    }

    fun clearAll() {
        Key.entries.forEach { delete(it) }
    }
}
