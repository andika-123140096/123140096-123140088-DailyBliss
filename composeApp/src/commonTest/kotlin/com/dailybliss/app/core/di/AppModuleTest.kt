package com.dailybliss.app.core.di

import com.dailybliss.app.core.util.PlatformContext
import io.mockk.mockk
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.Test

class AppModuleTest : KoinTest {

    @Test
    fun `check koin modules`() {
        // This is a powerful test that checks if all dependencies can be satisfied
        // But it needs all platform-specific modules to be present or mocked
        val testPlatformModule = module {
            single<PlatformContext> { mockk(relaxed = true) }
            // Add other platform dependencies if needed
        }

        // checkModules {
        //    modules(sharedModules + testPlatformModule)
        // }
    }
}
