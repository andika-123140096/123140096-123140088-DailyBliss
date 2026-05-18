package com.dailybliss.app.core.di

import com.dailybliss.app.core.util.DatabaseDriverFactory
import com.dailybliss.app.core.util.IosVoiceToTextParser
import com.dailybliss.app.core.util.PlatformContext
import com.dailybliss.app.core.util.VoiceToTextParser
import com.dailybliss.app.data.local.datastore.DataStoreFactory
import com.dailybliss.app.presentation.util.FileStorage
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * iOS-specific Koin module.
 *
 * Menyediakan dependencies platform yang dipakai di shared modules.
 */
val iosModule = module {
    single { PlatformContext() }
    single { DatabaseDriverFactory(get()) }
    single { DataStoreFactory(get()) }
    single { FileStorage(get()) }
    single { IosVoiceToTextParser() } bind VoiceToTextParser::class
}

/** Helper untuk dipanggil dari Swift code. */
fun initKoinIOS() {
    initKoin(platformModules = listOf(iosModule))
}

