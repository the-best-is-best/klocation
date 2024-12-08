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
    var isGPSNotOpen by mutableStateOf(false)

    var permissionLocation by mutableStateOf(false)

    fun init() {
        viewModelScope.launch {
            if (!permissionLocation) {
                controller.providePermission(Permission.LOCATION)
                permissionLocation = controller.isPermissionGranted(Permission.LOCATION)
            }
            // Request location permission if not already granted
            locationService.gpsStateFlow().collect { isGps ->
                isGPSNotOpen = isGps
            }

        }
//        viewModelScope.launch {
//
//            if(permissionLocation) {
//                locationService.startLocationUpdates().collect { newLocation ->
//                    userLocation = newLocation
//                }
//            }

        //        }
    }

    fun addListenerLocation() {
        viewModelScope.launch {
            if (controller.isPermissionGranted(Permission.LOCATION)) {
                locationService.startLocationUpdates().collect { newLocation ->
                    userLocation = newLocation
                }
            }
        }
    }


    fun enableLocation() {
        locationService.enableLocation()
    }
}
