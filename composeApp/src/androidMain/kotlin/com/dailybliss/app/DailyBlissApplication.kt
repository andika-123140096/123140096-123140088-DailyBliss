package com.dailybliss.app

import android.app.Application
import com.dailybliss.app.core.di.androidModule
import com.dailybliss.app.core.di.initKoin
import org.koin.android.ext.koin.androidContext

/**
 * Android Application class
 *
 * Entry point untuk inisialisasi app-wide dependencies.
 */
class DailyBlissApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Koin dengan platform module Android
        initKoin(
            platformModules = listOf(androidModule),
        ) {
            androidContext(this@DailyBlissApplication)
        }
    }
}
