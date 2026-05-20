package com.dailybliss.app.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.domain.usecase.GetAllMomentsUseCase
import com.dailybliss.app.domain.usecase.GetMomentsByDateRangeUseCase
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*

data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val mood: String? = null,
    val hasMoments: Boolean = false
)

data class CalendarUiState(
    val currentMonth: LocalDate, // Using first day of month
    val selectedDate: LocalDate,
    val calendarDays: List<CalendarDay> = emptyList(),
    val streak: Int = 0,
    val selectedDateMoments: List<Moment> = emptyList(),
    val isLoading: Boolean = true
)

class CalendarViewModel(
    private val getAllMomentsUseCase: GetAllMomentsUseCase
) : ViewModel() {

    private val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    private val _currentMonth = MutableStateFlow(now.let { LocalDate(it.year, it.month, 1) })
    private val _selectedDate = MutableStateFlow(now)

    private val _allMoments = getAllMomentsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<CalendarUiState> = combine(
        _currentMonth,
        _selectedDate,
        _allMoments
    ) { currentMonth, selectedDate, allMoments ->
        val days = generateCalendarDays(currentMonth, allMoments)
        val streak = calculateStreak(allMoments)
        
        // Filter moments for selected date
        val selectedMoments = allMoments.filter { 
            val date = it.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
            date == selectedDate
        }

        CalendarUiState(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            calendarDays = days,
            streak = streak,
            selectedDateMoments = selectedMoments,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState(now.let { LocalDate(it.year, it.month, 1) }, now))

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun nextMonth() {
        val current = _currentMonth.value
        _currentMonth.value = if (current.monthNumber == 12) {
            LocalDate(current.year + 1, 1, 1)
        } else {
            LocalDate(current.year, current.monthNumber + 1, 1)
        }
    }

    fun previousMonth() {
        val current = _currentMonth.value
        _currentMonth.value = if (current.monthNumber == 1) {
            LocalDate(current.year - 1, 12, 1)
        } else {
            LocalDate(current.year, current.monthNumber - 1, 1)
        }
    }

    private fun generateCalendarDays(monthDate: LocalDate, allMoments: List<Moment>): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        
        // Start from the first day of the month
        val firstDayOfMonth = LocalDate(monthDate.year, monthDate.month, 1)
        val dayOfWeekOfFirst = firstDayOfMonth.dayOfWeek.isoDayNumber // 1 (Mon) to 7 (Sun)
        
        // Padding from previous month (assuming we want to start from Monday)
        val paddingDays = dayOfWeekOfFirst - 1
        val startOfCalendar = firstDayOfMonth.minus(paddingDays, DateTimeUnit.DAY)
        
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        // Create 42 days (6 weeks) to keep the grid height consistent
        for (i in 0 until 42) {
            val date = startOfCalendar.plus(i, DateTimeUnit.DAY)
            val momentsOnDate = allMoments.filter { 
                it.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date == date 
            }
            
            days.add(
                CalendarDay(
                    date = date,
                    isCurrentMonth = date.month == monthDate.month && date.year == monthDate.year,
                    isToday = date == today,
                    mood = momentsOnDate.firstOrNull()?.mood, // Just show the first mood found for that day
                    hasMoments = momentsOnDate.isNotEmpty()
                )
            )
        }
        
        return days
    }

    private fun calculateStreak(allMoments: List<Moment>): Int {
        if (allMoments.isEmpty()) return 0
        
        val momentDates = allMoments.map { 
            it.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date 
        }.distinct().sortedDescending()
        
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val yesterday = today.minus(1, DateTimeUnit.DAY)
        
        var currentStreak = 0
        var checkDate = if (momentDates.contains(today)) today else if (momentDates.contains(yesterday)) yesterday else null
        
        if (checkDate == null) return 0
        
        while (momentDates.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate?.minus(1, DateTimeUnit.DAY)
        }
        
        return currentStreak
    }
}
