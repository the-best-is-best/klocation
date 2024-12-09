package io.github.tbib.klocation

import kotlinx.coroutines.flow.Flow

expect class KLocationService() {
    suspend fun getCurrentLocation(): Location
    fun startLocationUpdates(intervalMillis: Long = 10000): Flow<Location>
    fun enableLocation()
    fun isLocationEnabled(): Boolean
    fun gpsStateFlow(): Flow<Boolean>
//    fun hasLocationPermissionFlow(): StateFlow<Boolean>

}

data class Location(val latitude: Double, val longitude: Double)
