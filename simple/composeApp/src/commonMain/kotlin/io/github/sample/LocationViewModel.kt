package io.github.sample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.tbib.klocation.KLocationService
import io.github.tbib.klocation.Location
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {
    private val locationService = KLocationService()

    var userLocation by mutableStateOf<Location?>(null)
    var isGPSOpen by mutableStateOf(false)

    var requestPermission by mutableStateOf(false)




    fun init() {
        requestPermission = true

        viewModelScope.launch {


            // Request location permission if not already granted
                locationService.gpsStateFlow().collect { isGps ->
                    println("gps is $isGps")
                    isGPSOpen = isGps
                    addListenerLocation()
                }

        }
    }

    private fun addListenerLocation() {
        println("addListenerLocation has called $isGPSOpen")
        if (isGPSOpen) {
            viewModelScope.launch {
                locationService.startLocationUpdates().collect { newLocation ->
                    userLocation = newLocation
                }
            }
        }
    }

    suspend fun getLocation(): Location {
        return locationService.getCurrentLocation()
    }

    fun enableGPSAndLocation() {
        requestPermission = true
    }

    @Deprecated("use enableGPSAndLocation")
    fun enableLocation() {
        locationService.enableLocation()
    }
}
