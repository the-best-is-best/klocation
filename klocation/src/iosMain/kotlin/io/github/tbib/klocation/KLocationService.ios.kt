package io.github.tbib.klocation

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
import platform.Foundation.NSURL
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.darwin.NSObject

actual class KLocationService : NSObject(), CLLocationManagerDelegateProtocol {
    private val locationManager = CLLocationManager()
    private val locationUpdateChannel = MutableSharedFlow<Location>(replay = 1)

    private val _gpsStateFlow = MutableSharedFlow<Boolean>(replay = 1)
    private val locationPermissionState = MutableStateFlow(false)

    init {
        locationManager.delegate = this
        locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = (didUpdateLocations.lastOrNull() as? CLLocation)?.let {
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
        updateLocationPermissionState(locationManager)
        emitCurrentGpsState()
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        NSLog("Failed to retrieve location: ${didFailWithError.localizedDescription}")
    }


    actual suspend fun getCurrentLocation(): Location {
        locationManager.startUpdatingLocation()

        return try {
            locationUpdateChannel.first().also {
                locationManager.stopUpdatingLocation()
            }
        } catch (e: Exception) {
            NSLog("Error fetching location: ${e.message}")
            throw e
        }
    }

    actual fun startLocationUpdates(intervalMillis: Long): Flow<Location> = channelFlow {
        locationManager.startUpdatingLocation()
        try {
            locationUpdateChannel.collect { location ->
                send(location)
                delay(intervalMillis)
            }
        } catch (e: Exception) {
            NSLog("Error during location update collection: ${e.message}")
        } finally {
            locationManager.stopUpdatingLocation()
        }
    }

    actual fun isLocationEnabled(): Boolean {
        return CLLocationManager.locationServicesEnabled()
    }

    actual fun enableLocation() {
        when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusNotDetermined -> {
                locationManager.requestWhenInUseAuthorization()

            }

            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> {
                NSLog("Location permission already granted.")
                updateLocationPermissionState(locationManager)
            }

            else -> {
                println("open gps location")
                showAlertToPromptSettings()
            }
        }
    }

    private fun emitCurrentGpsState() {
        val isEnabled = CLLocationManager.locationServicesEnabled() &&
                (locationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways ||
                        locationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse)
        updateLocationPermissionState(locationManager)
        _gpsStateFlow.tryEmit(isEnabled)
    }

    override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: Int) {
        updateLocationPermissionState(manager)
        emitCurrentGpsState()
    }

    actual fun gpsStateFlow(): Flow<Boolean> = channelFlow {
        val initialState = CLLocationManager.locationServicesEnabled() &&
                (locationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways ||
                        locationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse)
        updateLocationPermissionState(locationManager)
        send(initialState)

        _gpsStateFlow.collect { isEnabled ->
            updateLocationPermissionState(locationManager)

            send(isEnabled)
        }
    }

//   actual fun hasLocationPermissionFlow(): StateFlow<Boolean> {
//        // Initial check for the authorization status
//        updateLocationPermissionState(locationManager)
//
//        // Return the StateFlow to be observed
//        return locationPermissionState
//    }

    // Method to update the permission state
    private fun updateLocationPermissionState(locationManager: CLLocationManager) {
        val status = locationManager.authorizationStatus
        locationPermissionState.value = status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                status == kCLAuthorizationStatusAuthorizedAlways
    }


}

fun showAlertToPromptSettings() {
    val topViewController = getTopViewController()
    if (topViewController != null) {
        val alert = UIAlertController.alertControllerWithTitle(
            "Location Services Disabled",
            "Location services are turned off. Please enable them in your device settings to use this feature.",
            UIAlertControllerStyleAlert
        )

        val goToSettingsAction = UIAlertAction.actionWithTitle(
            "Go to Settings",
            UIAlertActionStyleDefault
        ) { _ ->
            // Open the app-specific settings page
            val appSettingsURL = NSURL(string = "App-Prefs:root")
            UIApplication.sharedApplication.openURL(
                appSettingsURL,
                options = emptyMap<Any?, Any?>()
            ) {}

        }

        alert.addAction(goToSettingsAction)
        alert.addAction(UIAlertAction.actionWithTitle("Cancel", UIAlertActionStyleCancel, null))

        topViewController.presentViewController(alert, animated = true, completion = null)
    }
}
