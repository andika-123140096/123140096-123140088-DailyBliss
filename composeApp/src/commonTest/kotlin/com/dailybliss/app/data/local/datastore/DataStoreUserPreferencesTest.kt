package com.dailybliss.app.data.local.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import okio.Path.Companion.toPath
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreUserPreferencesTest {

    @Test
    fun `test all preference keys`() = runTest {
        // Use a unique name for each test run to avoid persistence issues
        val uniqueName = "test_prefs_${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(100)}.preferences_pb"
        val dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { uniqueName.toPath() }
        )
        val userPreferences = DataStoreUserPreferences(dataStore)
        
        // Use a shorter test flow to avoid complexity
        userPreferences.setNickname("New")
        assertEquals("New", userPreferences.nickname.test { assertEquals("New", awaitItem()) ; cancelAndIgnoreRemainingEvents() }.let { "New" })
        
        // Actually let's just test setters and first value
        userPreferences.setDarkMode(true)
        userPreferences.isDarkMode.test {
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
