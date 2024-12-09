package io.github.tbib.klocation

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

expect class KLocationService() {
    suspend fun getCurrentLocation(): Location
    fun startLocationUpdates(intervalMillis: Long = 10000): Flow<Location>

    @Deprecated("Use EnableLocation")
    fun enableLocation()

    @Composable
    fun EnableLocation()
    fun isLocationEnabled(): Boolean
    fun gpsStateFlow(): Flow<Boolean>


}

data class Location(val latitude: Double, val longitude: Double)
