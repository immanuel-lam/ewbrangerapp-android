package org.yac.llamarangers.demo

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persisted developer overrides for GPS spoofing. Demo build only.
 */
@Singleton
class DeveloperSettings @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("developer_settings", Context.MODE_PRIVATE)

    private val _spoofLocationEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_SPOOF_ENABLED, false)
    )
    val spoofLocationEnabled: StateFlow<Boolean> = _spoofLocationEnabled.asStateFlow()

    private val _spoofedPresetName = MutableStateFlow(
        prefs.getString(KEY_SPOOF_PRESET, LocationPreset.ALL.first().name)!!
    )
    val spoofedPresetName: StateFlow<String> = _spoofedPresetName.asStateFlow()

    /** Whether GPS spoofing is active. */
    var isSpoofingEnabled: Boolean
        get() = _spoofLocationEnabled.value
        set(value) {
            _spoofLocationEnabled.value = value
            prefs.edit().putBoolean(KEY_SPOOF_ENABLED, value).apply()
        }

    /** Name of the currently selected location preset. */
    var selectedPresetName: String
        get() = _spoofedPresetName.value
        set(value) {
            _spoofedPresetName.value = value
            prefs.edit().putString(KEY_SPOOF_PRESET, value).apply()
        }

    /** Returns the spoofed coordinate if spoofing is enabled, or null otherwise. */
    val spoofedCoordinate: Pair<Double, Double>?
        get() {
            if (!isSpoofingEnabled) return null
            val preset = LocationPreset.ALL.firstOrNull { it.name == selectedPresetName }
                ?: return null
            return Pair(preset.latitude, preset.longitude)
        }

    /** Convenience: spoofed latitude, or null if not spoofing. */
    val spoofedLatitude: Double?
        get() = spoofedCoordinate?.first

    /** Convenience: spoofed longitude, or null if not spoofing. */
    val spoofedLongitude: Double?
        get() = spoofedCoordinate?.second

    companion object {
        private const val KEY_SPOOF_ENABLED = "dev_spoofLocation"
        private const val KEY_SPOOF_PRESET = "dev_spoofPreset"
    }
}

// -----------------------------------------------------------------
// Location presets (zone & patrol area centroids in Port Stewart)
// -----------------------------------------------------------------

data class LocationPreset(
    val name: String,
    val latitude: Double,
    val longitude: Double,
) {
    companion object {
        val ALL: List<LocationPreset> = listOf(
            // Zone centroids
            LocationPreset("North Creek Gully", -14.685, 143.712),
            LocationPreset("Boundary Road East", -14.718, 143.698),
            LocationPreset("Homestead Track", -14.703, 143.722),
            LocationPreset("Rocky Point Scrub", -14.695, 143.683),
            LocationPreset("Mangrove Flat", -14.725, 143.715),
            LocationPreset("Station Dam", -14.710, 143.730),
            // Patrol area centroids
            LocationPreset("North Beach Dunes", -14.677, 143.702),
            LocationPreset("River Mouth Flats", -14.711, 143.722),
            LocationPreset("Camping Ground Perimeter", -14.700, 143.699),
            LocationPreset("Airstrip Corridor", -14.720, 143.690),
            LocationPreset("Creek Line East", -14.708, 143.730),
            LocationPreset("Central Clearing", -14.710, 143.700),
        )
    }
}
