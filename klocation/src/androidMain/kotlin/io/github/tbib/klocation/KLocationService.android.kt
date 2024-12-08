package io.github.tbib.klocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await


actual class KLocationService {

    private val context = AndroidKLocationService.getActivity()

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationFlow = MutableSharedFlow<Location>(replay = 1)
    private val _isLocationEnabledFlow = MutableSharedFlow<Boolean>(replay = 1)

    init {
        runBlocking {
            _isLocationEnabledFlow.tryEmit(isLocationEnabled())
        }
        // Register a BroadcastReceiver to monitor location service changes
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val isEnabled = isLocationEnabled()

                if (_isLocationEnabledFlow.replayCache.first() != isEnabled) {
                    _isLocationEnabledFlow.tryEmit(isEnabled)
                }
            }
        }, filter)
    }

    actual suspend fun getCurrentLocation(): Location {
        return try {
            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                Location(location.latitude, location.longitude)
            } else {
                throw Exception("Failed to get current location")
            }
        } catch (e: Exception) {
            Log.e("LocationService", "Error getting current location: ${e.message}")
            throw e
        }
    }

    actual fun startLocationUpdates(intervalMillis: Long): Flow<Location> {
        val locationRequest = LocationRequest.Builder(
            AndroidKLocationService.getAccuracyPriority().value,
            intervalMillis
        ).build()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    _locationFlow.tryEmit(Location(location.latitude, location.longitude))
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        return _locationFlow
    }


    actual fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            gpsEnabled || networkEnabled
        } catch (e: SecurityException) {
            Log.e("LocationService", "SecurityException: ${e.message}")
            false
        }
    }

    actual fun enableLocation() {
        if (!isLocationEnabled()) {
            val locationRequest = LocationRequest.Builder(
                AndroidKLocationService.getAccuracyPriority().value,
                10 * 1000 // Interval in milliseconds
            ).build()

            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            val client: SettingsClient = LocationServices.getSettingsClient(context)
            val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

            task.addOnSuccessListener {
                Log.d("LocationService", "Location settings are satisfied")
            }.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // Start resolution for enabling location
                    exception.startResolutionForResult(context, 1)
                } else {
                    Log.e("LocationService", "Location settings error: ${exception.message}")
                }
            }
        }
    }

    actual fun gpsStateFlow(): Flow<Boolean> {
        return _isLocationEnabledFlow.asSharedFlow()
    }
}
