package io.github.tbib.klocation

import platform.CoreLocation.CLLocationManager

class IOSKLocationServices {
    private val locationManager = CLLocationManager()

    fun requestPermission(isBackgroundSupport: Boolean = false) {
        if (isBackgroundSupport) {
            locationManager.requestAlwaysAuthorization()
        } else {
            locationManager.requestWhenInUseAuthorization()
        }
    }
}