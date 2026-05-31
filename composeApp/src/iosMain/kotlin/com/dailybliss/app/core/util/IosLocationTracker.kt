package com.dailybliss.app.core.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Implementasi iOS untuk LocationTracker menggunakan CLLocationManager.
 */
class IosLocationTracker : LocationTracker {
    private val locationManager = CLLocationManager()
    private var currentDelegate: CLLocationManagerDelegateProtocol? = null

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getCurrentLocation(): Location? = suspendCoroutine { continuation ->
        val status = locationManager.authorizationStatus
        if (status != kCLAuthorizationStatusAuthorizedWhenInUse && status != kCLAuthorizationStatusAuthorizedAlways) {
            continuation.resume(null)
            return@suspendCoroutine
        }

        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val location = didUpdateLocations.lastOrNull() as? CLLocation
                if (location != null) {
                    val result = location.coordinate.useContents {
                        Location(latitude = latitude, longitude = longitude)
                    }
                    manager.stopUpdatingLocation()
                    continuation.resume(result)
                } else {
                    continuation.resume(null)
                }
                currentDelegate = null
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: platform.Foundation.NSError) {
                continuation.resume(null)
                currentDelegate = null
            }
        }

        currentDelegate = delegate
        locationManager.delegate = delegate
        locationManager.requestLocation()
    }
}
