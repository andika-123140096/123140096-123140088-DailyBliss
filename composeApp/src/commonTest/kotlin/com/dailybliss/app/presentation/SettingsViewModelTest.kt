package com.dailybliss.app.presentation

import app.cash.turbine.test
import com.dailybliss.app.core.util.FakeBackgroundAIProcessor
import com.dailybliss.app.core.util.FakeNotifier
import com.dailybliss.app.data.local.datastore.FakeUserPreferences
import com.dailybliss.app.presentation.screens.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userPreferences: FakeUserPreferences
    private lateinit var backgroundAIProcessor: FakeBackgroundAIProcessor
    private lateinit var notifier: FakeNotifier
    private lateinit var viewModel: SettingsViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userPreferences = FakeUserPreferences()
        backgroundAIProcessor = FakeBackgroundAIProcessor()
        notifier = FakeNotifier()
        viewModel = SettingsViewModel(userPreferences, backgroundAIProcessor, notifier)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateNickname should update preferences and uiState and trigger AI`() = runTest {
        viewModel.uiState.test {
            assertEquals("User", awaitItem().nickname)

            viewModel.updateNickname("New Name")
            advanceUntilIdle()

            assertEquals("New Name", awaitItem().nickname)
            assertEquals("New Name", userPreferences.nickname.value)
            assertTrue(backgroundAIProcessor.updateGlobalSummaryCalled)
        }
    }

    @Test
    fun `toggleDarkMode should update preferences`() = runTest {
        viewModel.toggleDarkMode(true)
        advanceUntilIdle()
        assertEquals(true, userPreferences.isDarkMode.value)
    }

    @Test
    fun `updateAiStyle should update preferences and trigger AI`() = runTest {
        viewModel.updateAiStyle("Formal/Baku")
        advanceUntilIdle()
        assertEquals("Formal/Baku", userPreferences.aiLanguageStyle.value)
        assertTrue(backgroundAIProcessor.updateGlobalSummaryCalled)
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
