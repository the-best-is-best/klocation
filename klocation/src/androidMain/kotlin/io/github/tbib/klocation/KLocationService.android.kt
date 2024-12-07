package io.github.tbib.klocation

import android.content.Context
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

actual class KLocationService {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(AndroidKLocationService.getActivity())
    private val _locationFlow = MutableSharedFlow<Location>(replay = 1)
    private val locationFlow: SharedFlow<Location> = _locationFlow

    // Function to get the current location one time
    actual suspend fun getCurrentLocation(): Location {
        val location = runBlocking {
            fusedLocationClient.lastLocation.await() // This will suspend until the location is available

        }
        return if (location != null) {
            Location(location.latitude, location.longitude)
        } else {
            throw Exception("Failed to get current location")
        }
    }

    // Function to start location updates and emit them to a Flow
    actual fun startLocationUpdates(): Flow<Location> {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).apply {
                setMinUpdateIntervalMillis(5000)
            }.build()

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
        return locationFlow
    }

    actual fun isLocationEnabled(): Boolean {
        val context = AndroidKLocationService.getActivity()
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

}