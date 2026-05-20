package com.dailybliss.app.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.usecase.GetAllMomentsUseCase
import com.dailybliss.app.domain.usecase.GetMomentsFromSameDayUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val mood: String? = null,
    val hasMoments: Boolean = false,
)

data class CalendarUiState(
    val currentMonth: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.let { LocalDate(it.year, it.month, 1) },
    val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val days: List<CalendarDay> = emptyList(),
    val currentStreak: Int = 0,
    val isLoading: Boolean = false,
)

class CalendarViewModel(
    private val getAllMomentsUseCase: GetAllMomentsUseCase,
    private val getMomentsFromSameDayUseCase: GetMomentsFromSameDayUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Observe all moments to update calendar markers (moods, dots)
        viewModelScope.launch {
            getAllMomentsUseCase().collect { moments ->
                updateCalendarDays(moments)
                calculateStreak(moments)
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun nextMonth() {
        _uiState.update {
            val next = it.currentMonth.plus(1, DateTimeUnit.MONTH)
            it.copy(currentMonth = next)
        }
        viewModelScope.launch {
            updateCalendarDays(getAllMomentsUseCase().first())
        }
    }

    fun previousMonth() {
        _uiState.update {
            val prev = it.currentMonth.minus(1, DateTimeUnit.MONTH)
            it.copy(currentMonth = prev)
        }
        viewModelScope.launch {
            updateCalendarDays(getAllMomentsUseCase().first())
        }
    }

    private fun calculateStreak(moments: List<Moment>) {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        // Get unique dates that have moments
        val momentDates = moments.map { 
            it.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date 
        }.toSet()
        
        var streak = 0
        var currentDate = today
        
        // If no moment today, check if there was one yesterday. 
        // If not, streak is 0.
        if (!momentDates.contains(today)) {
            val yesterday = today.minus(1, DateTimeUnit.DAY)
            if (!momentDates.contains(yesterday)) {
                _uiState.update { it.copy(currentStreak = 0) }
                return
            }
            currentDate = yesterday
        }
        
        // Count backwards
        while (momentDates.contains(currentDate)) {
            streak++
            currentDate = currentDate.minus(1, DateTimeUnit.DAY)
        }
        
        _uiState.update { it.copy(currentStreak = streak) }
    }

    private fun updateCalendarDays(allMoments: List<Moment>) {
        val state = _uiState.value
        val firstDayOfMonth = state.currentMonth
        val daysInMonth = firstDayOfMonth.month.numberDays(firstDayOfMonth.year)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.isoDayNumber // 1 (Mon) to 7 (Sun)

        val days = mutableListOf<CalendarDay>()

        // Add days from previous month to fill the first week
        val prevMonth = firstDayOfMonth.minus(1, DateTimeUnit.MONTH)
        val daysInPrevMonth = prevMonth.month.numberDays(prevMonth.year)
        for (i in (firstDayOfWeek - 1) downTo 1) {
            val date = LocalDate(prevMonth.year, prevMonth.month, daysInPrevMonth - i + 1)
            days.add(createCalendarDay(date, allMoments, false))
        }

        // Add current month days
        for (i in 1..daysInMonth) {
            val date = LocalDate(firstDayOfMonth.year, firstDayOfMonth.month, i)
            days.add(createCalendarDay(date, allMoments, true))
        }

        // Add days from next month to fill the last week
        val nextMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH)
        val remainingDays = 42 - days.size
        for (i in 1..remainingDays) {
            val date = LocalDate(nextMonth.year, nextMonth.month, i)
            days.add(createCalendarDay(date, allMoments, false))
        }

        _uiState.update { it.copy(days = days) }
    }

    private fun createCalendarDay(date: LocalDate, allMoments: List<Moment>, isCurrentMonth: Boolean): CalendarDay {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        val momentsOnDate = allMoments.filter {
            val dt = it.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
            dt.date == date
        }

        return CalendarDay(
            date = date,
            isCurrentMonth = isCurrentMonth,
            isToday = date == today,
            mood = momentsOnDate.firstOrNull { !it.mood.isNullOrBlank() }?.mood?.split(" ")?.getOrNull(0),
            hasMoments = momentsOnDate.isNotEmpty(),
        )
    }

    private fun Month.numberDays(year: Int): Int = when (this) {
        Month.FEBRUARY -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        else -> 31
    }
}
