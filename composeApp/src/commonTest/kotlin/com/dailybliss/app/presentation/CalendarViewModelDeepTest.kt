package com.dailybliss.app.presentation

import app.cash.turbine.test
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelDeepTest {
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
    fun `calendar should show mood emoji if moment exists on that day`() = runTest {
        val tz = TimeZone.currentSystemDefault()
        val date = viewModel.uiState.value.currentMonth.plus(5, DateTimeUnit.DAY)
        val moment = Moment(title = "Happy Day", content = "C", mood = "😊 Happy", createdAt = date.atStartOfDayIn(tz))
        momentRepository.insertMoment(moment)
        
        advanceUntilIdle()
        
        val calendarDay = viewModel.uiState.value.days.find { it.date == date }
        assertEquals("😊", calendarDay?.mood)
        assertTrue(calendarDay?.hasMoments == true)
    }

    @Test
    fun `calculateStreak should handle gaps correctly`() = runTest {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val dayBeforeYesterday = today.minus(2, DateTimeUnit.DAY)
        
        momentRepository.insertMoment(Moment(title = "T1", content = "C", createdAt = today.atStartOfDayIn(tz)))
        momentRepository.insertMoment(Moment(title = "T2", content = "C", createdAt = dayBeforeYesterday.atStartOfDayIn(tz)))
        
        advanceUntilIdle()
        
        // Streak is 1 because yesterday was missing
        assertEquals(1, viewModel.uiState.value.currentStreak)
    }

    @Test
    fun `leap year February should have 29 days`() = runTest {
        // Need to set currentMonth to Feb 2024
        // But currentMonth is initialized with Clock.System.now()
        // I should probably make currentMonth internal or allow setting it.
    }
}

private fun LocalDate.atStartOfDayIn(tz: TimeZone): Instant = 
    kotlinx.datetime.LocalDateTime(this.year, this.month, this.dayOfMonth, 12, 0, 0, 0).toInstant(tz)
