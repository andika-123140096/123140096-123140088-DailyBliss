package com.dailybliss.app.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.darwin.NSObject

@Composable
actual fun LocationPermissionEffect(
    onPermissionResult: (Boolean) -> Unit
) {
    val locationManager = remember { CLLocationManager() }
    
    val delegate = remember {
        object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                val status = manager.authorizationStatus
                when (status) {
                    kCLAuthorizationStatusAuthorizedWhenInUse, 
                    kCLAuthorizationStatusAuthorizedAlways -> {
                        onPermissionResult(true)
                    }
                    kCLAuthorizationStatusNotDetermined -> {
                        // Still waiting
                    }
                    else -> {
                        onPermissionResult(false)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        locationManager.delegate = delegate
        val status = locationManager.authorizationStatus
        when (status) {
            kCLAuthorizationStatusAuthorizedWhenInUse, 
            kCLAuthorizationStatusAuthorizedAlways -> {
                onPermissionResult(true)
            }
            kCLAuthorizationStatusNotDetermined -> {
                locationManager.requestWhenInUseAuthorization()
            }
            else -> {
                onPermissionResult(false)
            }
        }
    }
}
