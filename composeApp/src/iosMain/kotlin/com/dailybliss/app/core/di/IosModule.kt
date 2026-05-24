package com.dailybliss.app.core.di

import com.dailybliss.app.core.network.ApiConfig
import com.dailybliss.app.core.network.IosApiConfig
import com.dailybliss.app.core.util.DatabaseDriverFactory
import com.dailybliss.app.core.util.IosDatabaseDriverFactory
import com.dailybliss.app.core.util.IosLocationTracker
import com.dailybliss.app.core.util.IosPlatformContext
import com.dailybliss.app.core.util.LocationTracker
import com.dailybliss.app.core.util.PlatformContext
import com.dailybliss.app.data.local.datastore.DataStoreFactory
import com.dailybliss.app.data.local.datastore.IosDataStoreFactory
import com.dailybliss.app.presentation.util.FileStorage
import com.dailybliss.app.presentation.util.IosFileStorage
import org.koin.dsl.module

/**
 * iOS-specific Koin module.
 *
 * Menyediakan dependencies platform yang dipakai di shared modules.
 */
val iosModule =
    module {
        single<ApiConfig> { IosApiConfig() }
        single<PlatformContext> { IosPlatformContext() }
        single<LocationTracker> { IosLocationTracker() }
        single<DatabaseDriverFactory> { IosDatabaseDriverFactory() }
        single<DataStoreFactory> { IosDataStoreFactory() }
        single<FileStorage> { IosFileStorage() }
    }

/** Helper untuk dipanggil dari Swift code. */
fun initKoinIOS() {
    initKoin(platformModules = listOf(iosModule))
}
