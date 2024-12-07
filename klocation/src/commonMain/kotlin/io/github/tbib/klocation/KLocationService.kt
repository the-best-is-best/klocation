package io.github.tbib.klocation

import kotlinx.coroutines.flow.Flow

expect class KLocationService() {
    suspend fun getCurrentLocation(): Location
    fun startLocationUpdates(): Flow<Location>
    fun isLocationEnabled(): Boolean
}

data class Location(val latitude: Double, val longitude: Double)
