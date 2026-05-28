package com.dailybliss.app.presentation

import com.dailybliss.app.data.repository.FakeMomentRepository
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.usecase.GetAllMomentsUseCase
import com.dailybliss.app.presentation.screens.calendar.CalendarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var momentRepository: FakeMomentRepository
    private lateinit var getAllMomentsUseCase: GetAllMomentsUseCase
    private lateinit var viewModel: CalendarViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        momentRepository = FakeMomentRepository()
        getAllMomentsUseCase = GetAllMomentsUseCase(momentRepository)

        viewModel = CalendarViewModel(getAllMomentsUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have 42 days in calendar`() = runTest {
        advanceUntilIdle()
        assertEquals(42, viewModel.uiState.value.days.size)
    }

    @Test
    fun `nextMonth should update currentMonth and days`() = runTest {
        val initialMonth = viewModel.uiState.value.currentMonth
        viewModel.nextMonth()
        advanceUntilIdle()

        val expectedMonth = initialMonth.plus(1, DateTimeUnit.MONTH)
        assertEquals(expectedMonth.year, viewModel.uiState.value.currentMonth.year)
        assertEquals(expectedMonth.month, viewModel.uiState.value.currentMonth.month)
    }

    @Test
    fun `previousMonth should update currentMonth and days`() = runTest {
        val initialMonth = viewModel.uiState.value.currentMonth
        viewModel.previousMonth()
        advanceUntilIdle()

        val expectedMonth = initialMonth.minus(1, DateTimeUnit.MONTH)
        assertEquals(expectedMonth.year, viewModel.uiState.value.currentMonth.year)
        assertEquals(expectedMonth.month, viewModel.uiState.value.currentMonth.month)
    }

    @Test
    fun `streak should be calculated correctly with today's moment`() = runTest {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val yesterday = today.minus(1, DateTimeUnit.DAY)

        momentRepository.insertMoment(Moment(title = "T1", content = "C", createdAt = today.atStartOfDayIn(tz)))
        momentRepository.insertMoment(Moment(title = "T2", content = "C", createdAt = yesterday.atStartOfDayIn(tz)))

        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.currentStreak)
    }

    @Test
    fun `streak should be calculated correctly with only yesterday's moment`() = runTest {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val yesterday = today.minus(1, DateTimeUnit.DAY)
        val dayBefore = yesterday.minus(1, DateTimeUnit.DAY)

        momentRepository.insertMoment(Moment(title = "T1", content = "C", createdAt = yesterday.atStartOfDayIn(tz)))
        momentRepository.insertMoment(Moment(title = "T2", content = "C", createdAt = dayBefore.atStartOfDayIn(tz)))

        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.currentStreak)
    }

    @Test
    fun `streak should be zero if no moment today or yesterday`() = runTest {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val threeDaysAgo = today.minus(3, DateTimeUnit.DAY)

        momentRepository.insertMoment(Moment(title = "T1", content = "C", createdAt = threeDaysAgo.atStartOfDayIn(tz)))

        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.currentStreak)
    }

    @Test
    fun `onDateSelected should update state`() = runTest {
        val date = LocalDate(2024, 1, 1)
        viewModel.onDateSelected(date)
        assertEquals(date, viewModel.uiState.value.selectedDate)
    }
}

private fun LocalDate.atStartOfDayIn(tz: TimeZone): Instant = kotlinx.datetime.LocalDateTime(this.year, this.month, this.dayOfMonth, 0, 0, 0, 0).toInstant(tz)
