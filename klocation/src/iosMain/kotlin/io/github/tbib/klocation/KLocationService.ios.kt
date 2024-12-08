package io.github.tbib.klocation

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLLocationAccuracyNearestTenMeters
import platform.Foundation.NSError
import platform.Foundation.NSLog
import platform.darwin.NSObject


actual class KLocationService : NSObject(), CLLocationManagerDelegateProtocol {
    private val locationManager = CLLocationManager()
    private val locationUpdateChannel = MutableSharedFlow<Location>(replay = 1)
    private val gpsStateFlow = MutableSharedFlow<Boolean>(replay = 1)

    init {
        locationManager.delegate = this
        locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = (didUpdateLocations.lastOrNull() as? CLLocation)?.let {
            // Using memScoped to handle the coordinate's pointer safely
            memScoped {
                val coordinatePtr = it.coordinate.ptr
                val latitude = coordinatePtr.pointed.latitude
                val longitude = coordinatePtr.pointed.longitude
                Location(latitude, longitude)
            }
        }

        location?.let { locationUpdateChannel.tryEmit(it) }
    }


    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        val isEnabled = CLLocationManager.locationServicesEnabled() &&
                (manager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways ||
                        manager.authorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse)
        gpsStateFlow.tryEmit(isEnabled)
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        NSLog("Failed to retrieve location: ${didFailWithError.localizedDescription}")
    }

    actual suspend fun getCurrentLocation(): Location {
        locationManager.startUpdatingLocation()

        val location = locationUpdateChannel.first()
        locationManager.stopUpdatingLocation()

        return location
    }

    actual fun startLocationUpdates(intervalMillis: Long): Flow<Location> = channelFlow {
        locationManager.startUpdatingLocation()
        locationUpdateChannel.collect { location ->
            send(location)
            delay(intervalMillis)
        }
        locationManager.stopUpdatingLocation()
    }

    actual fun isLocationEnabled(): Boolean {
        return CLLocationManager.locationServicesEnabled()
    }

    actual fun enableLocation() {
        when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusNotDetermined -> {
                locationManager.requestWhenInUseAuthorization()
                NSLog("Location permission requested.")
            }

            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> {
                NSLog("Location permission already granted.")
            }

            else -> {
                NSLog("Location permission denied or restricted.")
            }
        }
    }

    actual fun gpsStateFlow(): Flow<Boolean> {
        // Emit initial state
        val isEnabled = CLLocationManager.locationServicesEnabled() &&
                (locationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways ||
                        locationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse)
        gpsStateFlow.tryEmit(isEnabled)

        return gpsStateFlow
    }
}
