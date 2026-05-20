package com.dailybliss.app.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailybliss.app.presentation.components.EmptyState
import com.dailybliss.app.presentation.components.LoadingIndicator
import com.dailybliss.app.presentation.components.PremiumBlissCard
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToMomentDetail: (Long) -> Unit,
    viewModel: CalendarViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Kalender Kebahagiaan",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = (-0.5).sp
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Streak Card
                item {
                    StreakCard(streak = uiState.streak)
                }

                // Calendar Section
                item {
                    CalendarSection(
                        uiState = uiState,
                        onDateSelected = viewModel::onDateSelected,
                        onPrevMonth = viewModel::previousMonth,
                        onNextMonth = viewModel::nextMonth
                    )
                }

                // Moments for selected date
                item {
                    Text(
                        text = "Jurnal pada ${formatSelectedDate(uiState.selectedDate)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (uiState.selectedDateMoments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada jurnal pada hari ini.",
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(uiState.selectedDateMoments) { moment ->
                        PremiumBlissCard(
                            moment = moment,
                            onClick = { onNavigateToMomentDetail(moment.id) }
                        )
                    }
                }
                
                // Extra space at bottom
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun StreakCard(streak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔥", fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = "$streak Hari Beruntun!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                Text(
                    text = if (streak > 0) "Terus pertahankan kebiasaan baikmu!" else "Mulai tulis jurnal pertamamu hari ini!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun CalendarSection(
    uiState: CalendarUiState,
    onDateSelected: (LocalDate) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        // Month Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Prev")
            }
            Text(
                text = "${uiState.currentMonth.month.name} ${uiState.currentMonth.year}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Days of week
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "S", "R", "K", "J", "S", "M").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid
        val days = uiState.calendarDays
        val rows = days.chunked(7)
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEach { rowDays ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowDays.forEach { day ->
                        val isSelected = day.date == uiState.selectedDate
                        val textColor = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            !day.isCurrentMonth -> Color.LightGray
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.8f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onDateSelected(day.date) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = day.date.dayOfMonth.toString(),
                                color = textColor,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            if (day.hasMoments) {
                                val moodEmoji = day.mood?.split(" ")?.getOrNull(0) ?: "•"
                                Text(
                                    text = moodEmoji,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                                )
                            } else {
                                // Empty space to keep alignment
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatSelectedDate(date: LocalDate): String {
    return "${date.dayOfMonth} ${date.month.name} ${date.year}"
}
