package com.dailybliss.app.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat

/**
 * Implementasi Android untuk LocationTracker menggunakan LocationManager.
 */
actual class LocationTracker actual constructor(private val context: PlatformContext) {
    actual suspend fun getCurrentLocation(): Location? {
        val androidContext = context.androidContext
        
        // Cek izin lokasi
        val hasCoarsePermission = ContextCompat.checkSelfPermission(
            androidContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasFinePermission = ContextCompat.checkSelfPermission(
            androidContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCoarsePermission && !hasFinePermission) {
            return null
        }

        val locationManager = androidContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        // Ambil list provider yang aktif (gps, network, dll)
        val providers = locationManager.getProviders(true)
        var bestLocation: android.location.Location? = null
        
        for (provider in providers) {
            val l = try {
                locationManager.getLastKnownLocation(provider)
            } catch (e: SecurityException) {
                null
            } ?: continue
            
            if (bestLocation == null || l.accuracy < bestLocation!!.accuracy) {
                bestLocation = l
            }
        }
        
        return bestLocation?.let { 
            Location(latitude = it.latitude, longitude = it.longitude) 
        }
    }
}
