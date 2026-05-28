package com.dailybliss.app.presentation

import app.cash.turbine.test
import com.dailybliss.app.core.util.FakeBackgroundAIProcessor
import com.dailybliss.app.data.local.datastore.FakeUserPreferences
import com.dailybliss.app.data.repository.FakeMomentRepository
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.presentation.screens.home.HomeViewModel
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var momentRepository: FakeMomentRepository
    private lateinit var userPreferences: FakeUserPreferences
    private lateinit var backgroundAIProcessor: FakeBackgroundAIProcessor
    private lateinit var viewModel: HomeViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        momentRepository = FakeMomentRepository()
        userPreferences = FakeUserPreferences()
        backgroundAIProcessor = FakeBackgroundAIProcessor()
        
        viewModel = HomeViewModel(
            momentRepository = momentRepository,
            userPreferences = userPreferences,
            backgroundAIProcessor = backgroundAIProcessor
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should load data correctly from dependencies`() = runTest {
        userPreferences.setNickname("Andika")
        userPreferences.setDailyInsight("Semangat!")
        momentRepository.insertMoment(Moment(title = "M1", content = "C1", mood = "Senang 😊"))

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            
            assertEquals("Andika", state.nickname)
            assertEquals("Semangat!", state.dailyInsight)
            assertEquals(1, state.totalMoments)
            assertEquals(1, state.moodStats["😊"])
            assertEquals(false, state.isLoading)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isAiProcessing should react to backgroundAIProcessor`() = runTest {
        backgroundAIProcessor.setProcessing(true)
        
        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state.isAiProcessing)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
