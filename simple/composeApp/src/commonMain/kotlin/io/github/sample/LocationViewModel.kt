package io.github.sample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import io.github.tbib.klocation.KLocationService
import io.github.tbib.klocation.Location
import kotlinx.coroutines.launch

class LocationViewModel(private val controller: PermissionsController) : ViewModel() {
    private val locationService = KLocationService()

    var userLocation by mutableStateOf<Location?>(null)
    var isGPSOpen by mutableStateOf(false)

    var permissionLocation by mutableStateOf(false)

    fun init() {
        viewModelScope.launch {

            try {
                controller.providePermission(Permission.LOCATION)
                permissionLocation = controller.isPermissionGranted(Permission.LOCATION)
            } catch (e: Exception) {

            }
        }
        viewModelScope.launch {


            // Request location permission if not already granted
            locationService.gpsStateFlow().collect { isGps ->
                println("gps is $isGps")
                if (isGps) {
                    addListenerLocation()
                }
                isGPSOpen = isGps
            }

        }
    }

    private fun addListenerLocation() {
        println("addListenerLocation has called")
        viewModelScope.launch {
            locationService.startLocationUpdates().collect { newLocation ->
                userLocation = newLocation
            }
        }
    }

    suspend fun getLocation(): Location {
        return locationService.getCurrentLocation()
    }


    fun enableLocation() {
        locationService.enableLocation()
    }
}
