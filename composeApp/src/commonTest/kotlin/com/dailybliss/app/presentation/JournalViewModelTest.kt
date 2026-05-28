package com.dailybliss.app.presentation

import app.cash.turbine.test
import com.dailybliss.app.data.repository.FakeMomentRepository
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.usecase.GetAllMomentsUseCase
import com.dailybliss.app.presentation.screens.home.JournalUiState
import com.dailybliss.app.presentation.screens.home.JournalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var momentRepository: FakeMomentRepository
    private lateinit var getAllMomentsUseCase: GetAllMomentsUseCase
    private lateinit var viewModel: JournalViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        momentRepository = FakeMomentRepository()
        getAllMomentsUseCase = GetAllMomentsUseCase(momentRepository)
        viewModel = JournalViewModel(getAllMomentsUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Success or Empty based on repository`() = runTest {
        viewModel.uiState.test {
            assertEquals(JournalUiState.Loading, awaitItem())

            // Advance time to pass debounce if needed (initially empty query might not debounce if emitted immediately)
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is JournalUiState.Empty)

            momentRepository.insertMoment(Moment(title = "T", content = "C"))

            val successState = awaitItem()
            assertTrue(successState is JournalUiState.Success)
            assertEquals(1, (successState as JournalUiState.Success).moments.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search should filter moments with debounce`() = runTest {
        momentRepository.insertMoment(Moment(title = "Matching", content = "C"))
        momentRepository.insertMoment(Moment(title = "Other", content = "C"))

        viewModel.uiState.test {
            skipItems(2) // Loading + Empty/Initial Success

            viewModel.onSearchQueryChange("Matching")

            // Debounce is 300ms
            advanceTimeBy(301L)
            runCurrent()

            val state = awaitItem()
            assertTrue(state is JournalUiState.Success)
            assertEquals(1, state.moments.size)
            assertEquals("Matching", state.moments[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
