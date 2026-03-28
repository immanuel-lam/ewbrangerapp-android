package org.yac.llamarangers.service.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Location service using FusedLocationProviderClient.
 * Ports iOS LocationManager with 5-second timeout and Port Stewart fallback.
 */
@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    enum class AccuracyLevel(val color: String) {
        GOOD("green"),   // < 10m
        FAIR("yellow"),  // 10-50m
        POOR("red"),     // > 50m
        UNKNOWN("gray")
    }

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _accuracyLevel = MutableStateFlow(AccuracyLevel.UNKNOWN)
    val accuracyLevel: StateFlow<AccuracyLevel> = _accuracyLevel.asStateFlow()

    private var locationCallback: LocationCallback? = null

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startUpdating() {
        if (!hasLocationPermission()) return
        stopUpdating()

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateDistanceMeters(5f)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                _currentLocation.value = location
                _accuracyLevel.value = classifyAccuracy(location.accuracy)
            }
        }
        locationCallback = callback
        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }

    fun stopUpdating() {
        locationCallback?.let { fusedClient.removeLocationUpdates(it) }
        locationCallback = null
    }

    /**
     * Captures a single high-accuracy location, then stops.
     * Falls back to Port Stewart default after 5 seconds (handles emulator + poor signal).
     */
    @SuppressLint("MissingPermission")
    suspend fun captureLocation(): Location {
        if (!hasLocationPermission()) return portStewartFallback()

        val result = withTimeoutOrNull(5_000L) {
            suspendCancellableCoroutine { cont ->
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_000L)
                    .setMaxUpdates(10)
                    .build()

                val callback = object : LocationCallback() {
                    private var resumed = false

                    override fun onLocationResult(result: LocationResult) {
                        val location = result.lastLocation ?: return
                        _currentLocation.value = location
                        _accuracyLevel.value = classifyAccuracy(location.accuracy)

                        // Accept if accuracy is reasonable (< 30m)
                        if (!resumed && location.accuracy < 30f) {
                            resumed = true
                            fusedClient.removeLocationUpdates(this)
                            cont.resume(location)
                        }
                    }
                }

                fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

                cont.invokeOnCancellation {
                    fusedClient.removeLocationUpdates(callback)
                }
            }
        }

        return result ?: portStewartFallback()
    }

    private fun portStewartFallback(): Location {
        // Default: Port Stewart, Cape York (-14.7019, 143.7075)
        val fallback = Location("fallback").apply {
            latitude = PORT_STEWART_LAT
            longitude = PORT_STEWART_LON
            altitude = 5.0
            accuracy = 50f
            time = System.currentTimeMillis()
        }
        _currentLocation.value = fallback
        _accuracyLevel.value = AccuracyLevel.FAIR
        return fallback
    }

    private fun classifyAccuracy(accuracy: Float): AccuracyLevel {
        return when {
            accuracy < 10f -> AccuracyLevel.GOOD
            accuracy < 50f -> AccuracyLevel.FAIR
            else -> AccuracyLevel.POOR
        }
    }

    companion object {
        const val PORT_STEWART_LAT = -14.7019
        const val PORT_STEWART_LON = 143.7075
    }
}
