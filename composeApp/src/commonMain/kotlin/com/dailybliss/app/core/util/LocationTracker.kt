package com.dailybliss.app.core.util

/**
 * Data class untuk merepresentasikan koordinat lokasi.
 */
data class Location(
    val latitude: Double,
    val longitude: Double
)

/**
 * Expect class untuk mengambil lokasi GPS aktual dari platform.
 */
expect class LocationTracker(context: PlatformContext) {
    /**
     * Mengambil lokasi GPS saat ini.
     * @return [Location] jika berhasil dan izin diberikan, null jika gagal atau izin ditolak.
     */
    suspend fun getCurrentLocation(): Location?
}
