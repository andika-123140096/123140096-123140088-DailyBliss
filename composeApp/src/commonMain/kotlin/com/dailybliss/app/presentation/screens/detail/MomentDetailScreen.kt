package com.dailybliss.app.presentation.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailybliss.app.presentation.components.HtmlBlockItem
import com.dailybliss.app.presentation.components.LoadingIndicator
import kotlinx.datetime.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MomentDetailScreen(
    momentId: Long,
    onNavigateBack: () -> Unit,
    viewModel: MomentDetailViewModel = koinViewModel { parametersOf(momentId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Detail Jurnal",
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
                actions = {
                    val isPinned = uiState.moment?.isPinned == true
                    IconButton(onClick = { viewModel.togglePin() }) {
                        Icon(
                            if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            "Pin",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        )
                    }
                    IconButton(onClick = { viewModel.refreshAIAnalysis() }) {
                        Icon(Icons.Outlined.Refresh, "Refresh AI", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            uiState.moment?.let { moment ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    Text(
                        text = moment.title.ifBlank { "Momen Tanpa Judul" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val dateStr = moment.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).let {
                        "\${it.dayOfMonth} \${it.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }} \${it.year}"
                    }
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mood & Tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        moment.mood?.let { mood ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(mood) },
                                shape = RoundedCornerShape(12.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            moment.tags.forEach { tag ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text("#$tag") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        labelColor = MaterialTheme.colorScheme.secondary,
                                    ),
                                    border = null,
                                    shape = RoundedCornerShape(12.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    HtmlBlockItem(
                        html = moment.content,
                        onHtmlChange = {}, // Read-only in detail
                        onFocusValueChange = { _, _, _ -> },
                        enabled = false,
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Jurnal") },
            text = { Text("Apakah kamu yakin ingin menghapus jurnal ini? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMoment {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            },
        )
    }
}
