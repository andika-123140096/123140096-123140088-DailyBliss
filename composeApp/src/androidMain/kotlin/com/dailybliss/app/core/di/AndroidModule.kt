package com.dailybliss.app.core.di

import com.dailybliss.app.core.util.AndroidVoiceToTextParser
import com.dailybliss.app.core.util.DatabaseDriverFactory
import com.dailybliss.app.core.util.VoiceToTextParser
import com.dailybliss.app.data.local.datastore.DataStoreFactory
import com.dailybliss.app.presentation.util.FileStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

import com.dailybliss.app.core.util.PlatformContext

/**
 * Android-specific Koin module.
 *
 * Menyediakan dependencies yang membutuhkan `Context`:
 * - DatabaseDriverFactory: untuk SQLDelight driver
 * - DataStoreFactory     : untuk lokasi file preferences
 * - FileStorage          : untuk menyimpan file gambar
 * - VoiceToTextParser    : untuk Speech-to-Text
 */
val androidModule = module {
    single { PlatformContext(androidContext()) }
    single { DatabaseDriverFactory(get()) }
    single { DataStoreFactory(get()) }
    single { FileStorage(get()) }
    single { AndroidVoiceToTextParser(get()) } bind VoiceToTextParser::class
}

