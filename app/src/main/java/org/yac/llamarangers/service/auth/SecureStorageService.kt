package org.yac.llamarangers.service.auth

import android.content.Context
import android.content.SharedPreferences
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
    enum class Key(val value: String) {
        JWT("org.yac.llamarangers.jwt"),
        REFRESH_TOKEN("org.yac.llamarangers.refresh"),
        PIN("org.yac.llamarangers.pin"),
        RANGER_ID("org.yac.llamarangers.rangerID")
    }

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "org.yac.llamarangers.secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun save(key: Key, value: String): Boolean {
        return prefs.edit().putString(key.value, value).commit()
    }

    fun load(key: Key): String? {
        return prefs.getString(key.value, null)
    }

    fun delete(key: Key): Boolean {
        return prefs.edit().remove(key.value).commit()
    }

    fun clearAll() {
        Key.entries.forEach { delete(it) }
    }
}
