package com.dailybliss.app.core.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse

/**
 * Implementasi iOS untuk LocationTracker menggunakan CLLocationManager.
 */
actual class LocationTracker actual constructor(private val context: PlatformContext) {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getCurrentLocation(): Location? {
        val locationManager = CLLocationManager()
        
        val status = locationManager.authorizationStatus
        if (status != kCLAuthorizationStatusAuthorizedWhenInUse && status != kCLAuthorizationStatusAuthorizedAlways) {
            return null
        }

        val location = locationManager.location
        return location?.coordinate?.useContents {
            Location(latitude = latitude, longitude = longitude)
        }
    }
}
