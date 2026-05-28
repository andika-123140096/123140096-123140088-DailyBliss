package com.dailybliss.app.presentation.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailybliss.app.presentation.components.BlissCard
import com.dailybliss.app.presentation.components.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyMomentsScreen(
    dateStr: String,
    onNavigateBack: () -> Unit,
    onNavigateToMomentDetail: (Long) -> Unit,
    viewModel: DailyMomentsViewModel = koinViewModel(parameters = { parametersOf(dateStr) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DailyMomentsScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToMomentDetail = onNavigateToMomentDetail,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyMomentsScreenContent(
    uiState: DailyMomentsUiState,
    onNavigateBack: () -> Unit,
    onNavigateToMomentDetail: (Long) -> Unit,
) {
    Scaffold(
        modifier = Modifier.testTag("DAILY_MOMENTS_SCREEN"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val date = uiState.date
                    Text(
                        "${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("BACK_BUTTON")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.testTag("LOADING_INDICATOR")) {
                    LoadingIndicator()
                }
            } else if (uiState.moments.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp).testTag("EMPTY_STATE"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "Tidak ada jurnal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        "Kamu belum menulis apapun di tanggal ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("MOMENT_LIST"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(uiState.moments) { moment ->
                        Box(modifier = Modifier.testTag("MOMENT_ITEM_${moment.id}")) {
                            BlissCard(
                                moment = moment,
                                onClick = { onNavigateToMomentDetail(moment.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
