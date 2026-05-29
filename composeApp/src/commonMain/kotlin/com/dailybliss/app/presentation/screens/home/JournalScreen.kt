package com.dailybliss.app.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailybliss.app.presentation.components.BlissCard
import com.dailybliss.app.presentation.components.EmptyState
import com.dailybliss.app.presentation.components.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onNavigateToCreateMoment: () -> Unit,
    onNavigateToMomentDetail: (Long) -> Unit,
    onNavigateBack: () -> Unit, // Added to match actions
    viewModel: JournalViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    JournalScreenContent(
        uiState = uiState,
        query = query,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onClearSearch = viewModel::clearSearch,
        onLoadMore = viewModel::loadMore,
        onNavigateToCreateMoment = onNavigateToCreateMoment,
        onNavigateToMomentDetail = onNavigateToMomentDetail,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreenContent(
    uiState: JournalUiState,
    query: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onNavigateToCreateMoment: () -> Unit,
    onNavigateToMomentDetail: (Long) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val listState = rememberLazyListState()

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount == 0) {
                false
            } else {
                val lastVisibleItem = visibleItemsInfo.lastOrNull()
                lastVisibleItem != null && (lastVisibleItem.index + 1 >= layoutInfo.totalItemsCount)
            }
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom && uiState is JournalUiState.Success && !uiState.isLastPage) {
            onLoadMore()
        }
    }

    Scaffold(
        modifier = Modifier.testTag("JOURNAL_SCREEN"),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Jurnal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = (-0.5).sp,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateMoment,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp),
                modifier = Modifier.testTag("ADD_MOMENT_FAB"),
            ) {
                Icon(Icons.Default.Add, "Add", modifier = Modifier.size(24.dp))
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("SEARCH_TEXT_FIELD"),
                placeholder = { Text("Cari jurnal...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = onClearSearch,
                            modifier = Modifier.testTag("CLEAR_SEARCH_BUTTON"),
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                ),
            )

            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                when (val state = uiState) {
                    is JournalUiState.Loading -> LoadingIndicator()
                    is JournalUiState.Success -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("JOURNAL_LIST"),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(
                                items = state.moments,
                                key = { it.id },
                            ) { moment ->
                                Box(modifier = Modifier.testTag("JOURNAL_ITEM_${moment.id}")) {
                                    BlissCard(
                                        moment = moment,
                                        onClick = { onNavigateToMomentDetail(moment.id) },
                                    )
                                }
                            }

                            if (!state.isLastPage) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is JournalUiState.Empty -> {
                        Box(modifier = Modifier.testTag("EMPTY_STATE")) {
                            EmptyState(
                                title = if (query.isNotEmpty()) "Tidak Ditemukan" else "Mulai Menulis",
                                message = if (query.isNotEmpty()) "Tidak ada jurnal yang sesuai dengan kata kunci '$query'." else "Ceritakan hal-hal kecil yang membuatmu tersenyum hari ini.",
                            )
                        }
                    }
                    is JournalUiState.Error -> {
                        Text(
                            text = "Error: ${state.message}",
                            modifier = Modifier
                                .padding(24.dp)
                                .testTag("ERROR_MESSAGE"),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
