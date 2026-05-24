package com.dailybliss.app.core.di

import com.dailybliss.app.core.network.AndroidApiConfig
import com.dailybliss.app.core.network.ApiConfig
import com.dailybliss.app.core.util.AndroidDatabaseDriverFactory
import com.dailybliss.app.core.util.AndroidLocationTracker
import com.dailybliss.app.core.util.AndroidPlatformContext
import com.dailybliss.app.core.util.DatabaseDriverFactory
import com.dailybliss.app.core.util.LocationTracker
import com.dailybliss.app.core.util.PlatformContext
import com.dailybliss.app.data.local.datastore.AndroidDataStoreFactory
import com.dailybliss.app.data.local.datastore.DataStoreFactory
import com.dailybliss.app.presentation.util.AndroidFileStorage
import com.dailybliss.app.presentation.util.FileStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific Koin module.
 *
 * Menyediakan dependencies yang membutuhkan `Context`:
 * - DatabaseDriverFactory: untuk SQLDelight driver
 * - DataStoreFactory     : untuk lokasi file preferences
 * - FileStorage          : untuk menyimpan file gambar
 */
val androidModule =
    module {
        single<ApiConfig> { AndroidApiConfig() }
        single<PlatformContext> { AndroidPlatformContext(androidContext()) }
        single<LocationTracker> { AndroidLocationTracker(get()) }
        single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(get()) }
        single<DataStoreFactory> { AndroidDataStoreFactory(get()) }
        single<FileStorage> { AndroidFileStorage(get()) }
    }
