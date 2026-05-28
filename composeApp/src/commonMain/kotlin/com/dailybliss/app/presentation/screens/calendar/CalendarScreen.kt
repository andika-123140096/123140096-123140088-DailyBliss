package com.dailybliss.app.presentation.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailybliss.app.presentation.components.CalendarView
import org.koin.compose.viewmodel.koinViewModel

import androidx.compose.ui.platform.testTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToDailyMoments: (String) -> Unit,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalendarScreenContent(
        uiState = uiState,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onDateSelected = { date ->
            viewModel.onDateSelected(date)
            val dateStr = "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
            onNavigateToDailyMoments(dateStr)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreenContent(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (kotlinx.datetime.LocalDate) -> Unit,
) {
    Scaffold(
        modifier = Modifier.testTag("CALENDAR_SCREEN"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Kalender",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = (-0.5).sp,
                        ),
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
        ) {
            // Month Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier.testTag("PREV_MONTH_BUTTON")
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous")
                }

                Text(
                    text = "${uiState.currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${uiState.currentMonth.year}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.testTag("CURRENT_MONTH_TEXT")
                )

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.testTag("NEXT_MONTH_BUTTON")
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next")
                }
            }

            // Streak Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("STREAK_CARD"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 24.sp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${uiState.currentStreak} Hari Beruntun Menulis",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }

            // Calendar View
            CalendarView(
                days = uiState.days,
                selectedDate = uiState.selectedDate,
                onDateSelected = onDateSelected,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
