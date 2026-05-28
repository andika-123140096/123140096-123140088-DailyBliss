package com.dailybliss.app.presentation

import app.cash.turbine.test
import com.dailybliss.app.data.local.datastore.FakeUserPreferences
import com.dailybliss.app.presentation.screens.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userPreferences: FakeUserPreferences
    private lateinit var viewModel: SettingsViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userPreferences = FakeUserPreferences()
        viewModel = SettingsViewModel(userPreferences)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateNickname should update preferences and uiState`() = runTest {
        viewModel.uiState.test {
            assertEquals("User", awaitItem().nickname)

            viewModel.updateNickname("New Name")
            advanceUntilIdle()

            assertEquals("New Name", awaitItem().nickname)
            assertEquals("New Name", userPreferences.nickname.value)
        }
    }

    @Test
    fun `toggleDarkMode should update preferences`() = runTest {
        viewModel.toggleDarkMode(true)
        advanceUntilIdle()
        assertEquals(true, userPreferences.isDarkMode.value)
    }

    @Test
    fun `updateAiStyle should update preferences`() = runTest {
        viewModel.updateAiStyle("Formal/Baku")
        advanceUntilIdle()
        assertEquals("Formal/Baku", userPreferences.aiLanguageStyle.value)
    }

    @Test
    fun `initial state should load summary and insight`() = runTest {
        userPreferences.setJournalSummary("Summary")
        userPreferences.setDailyInsight("Insight")

        // Re-create ViewModel to pick up new values if needed,
        // though it should be reactive if using Flow correctly.
        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals("Summary", state.journalSummary)
            assertEquals("Insight", state.dailyInsight)
        }
    }
}
