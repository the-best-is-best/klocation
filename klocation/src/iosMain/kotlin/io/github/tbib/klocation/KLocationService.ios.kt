package io.github.tbib.klocation

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyNearestTenMeters
import platform.Foundation.NSError
import platform.darwin.NSObject

actual class KLocationService : NSObject(), CLLocationManagerDelegateProtocol {
    private val locationManager = CLLocationManager()
    private val locationUpdateChannel = Channel<Pair<Double, Double>>(Channel.UNLIMITED)

    init {
        locationManager.delegate = this
        locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
        locationManager.startUpdatingLocation()
    }


    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(
        manager: CLLocationManager,
        didUpdateLocations: List<*>
    ) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation
        location?.let {
            memScoped {
                val coordinate = it.coordinate.ptr
                val latitude = coordinate.pointed.latitude
                val longitude = coordinate.pointed.longitude

                // Emit the updated location to the channel
                locationUpdateChannel.trySend(Pair(latitude, longitude)).isSuccess
            }
        }
    }

    override fun locationManager(
        manager: CLLocationManager,
        didFailWithError: NSError
    ) {
        println("Failed to retrieve location: ${didFailWithError.localizedDescription}")
        // Optionally, close the channel to indicate an error state if necessary
        locationUpdateChannel.close()
    }

    // Function to get the current location one-time
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getCurrentLocation(): Location {
        val deferred = CompletableDeferred<Location>()

        // Start a coroutine to receive the location asynchronously
        locationManager.delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val location = didUpdateLocations.lastOrNull() as? CLLocation
                location?.let {
                    memScoped {
                        val coordinate = it.coordinate.ptr
                        val latitude = coordinate.pointed.latitude
                        val longitude = coordinate.pointed.longitude

                        deferred.complete(Location(latitude, longitude))
                        locationManager.stopUpdatingLocation() // Stop updates after receiving the first location
                    }
                }
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                deferred.completeExceptionally(Exception("Failed to retrieve location: ${didFailWithError.localizedDescription}"))
            }
        }

        return deferred.await() // This will block until the result is received
    }


    // Function to start location updates and emit them to a Flow
    actual fun startLocationUpdates(): Flow<Location> = channelFlow {
        // Continuously receive location updates from the channel and emit them to the flow
        for (location in locationUpdateChannel) {
            send(Location(location.first, location.second))
        }
        // Close the flow when the channel is closed
        close()
    }
}
