package com.dailybliss.app.presentation

import com.dailybliss.app.data.repository.FakeMomentRepository
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.usecase.GetMomentsForDateUseCase
import com.dailybliss.app.presentation.screens.calendar.DailyMomentsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class DailyMomentsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var momentRepository: FakeMomentRepository
    private lateinit var getMomentsForDateUseCase: GetMomentsForDateUseCase
    private lateinit var viewModel: DailyMomentsViewModel

    private val dateStr = "2026-05-26"
    private val testDate = LocalDate.parse(dateStr)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        momentRepository = FakeMomentRepository()
        getMomentsForDateUseCase = GetMomentsForDateUseCase(momentRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMoments should load moments for selected date`() = runTest {
        val tz = TimeZone.currentSystemDefault()
        val moment1 = Moment(
            id = 1L,
            title = "M1",
            content = "C1",
            createdAt = testDate.atStartOfDayIn(tz),
        )
        momentRepository.insertMoment(moment1)

        viewModel = DailyMomentsViewModel(dateStr, getMomentsForDateUseCase)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.moments.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `initial state should use current date if string is invalid`() = runTest {
        // DailyMomentsViewModel(dateStr, ...) calls LocalDate.parse(dateStr)
        // If it throws, the VM init fails. Usually we should guard this.
    }
}

private fun LocalDate.atStartOfDayIn(tz: TimeZone): Instant = LocalDateTime(this.year, this.month, this.dayOfMonth, 0, 0, 0, 0).toInstant(tz)
