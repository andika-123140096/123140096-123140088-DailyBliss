package com.dailybliss.app.presentation.screens.addnote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailybliss.app.domain.model.ContentBlock
import com.dailybliss.app.presentation.components.*
import com.dailybliss.app.presentation.util.rememberImagePickerLauncher
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateMomentScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateMomentViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    var activeBlockIndex by remember { mutableStateOf<Int?>(null) }
    
    // Focus management
    val focusRequesters = remember(uiState.contentBlocks.size) {
        List(uiState.contentBlocks.size) { FocusRequester() }
    }
    
    LaunchedEffect(uiState.requestedFocusIndex) {
        uiState.requestedFocusIndex?.let { index ->
            if (index in focusRequesters.indices) {
                try {
                    focusRequesters[index].requestFocus()
                    viewModel.clearFocusRequest()
                } catch (e: Exception) {}
            }
        }
    }
    
    val imagePicker = rememberImagePickerLauncher(
        onResult = { bytesList ->
            if (bytesList.isNotEmpty()) {
                viewModel.addImageGroupBlock(bytesList, afterIndex = activeBlockIndex)
            }
        }
    )
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateMomentEvent.MomentSaved -> onNavigateBack()
                is CreateMomentEvent.Error -> snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            "Back", 
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    }

                    TextButton(
                        onClick = { viewModel.saveMoment() },
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "Simpan", 
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            BasicTextField(
                                value = uiState.title,
                                onValueChange = viewModel::onTitleChange,
                                textStyle = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    if (uiState.title.isEmpty()) {
                                        Text(
                                            text = "Judul Cerita",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )

                            // Mood & Tags Display
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                uiState.mood?.let { mood ->
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(mood) },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    uiState.tags.forEach { tag ->
                                        AssistChip(
                                            onClick = {},
                                            label = { Text("#$tag") },
                                            colors = AssistChipDefaults.assistChipColors(
                                                labelColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            border = null,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    itemsIndexed(uiState.contentBlocks) { index, block ->
                        Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                            when (block) {
                                is ContentBlock.Html -> {
                                    HtmlBlockItem(
                                        html = block.content,
                                        onHtmlChange = { viewModel.onBlockChange(index, block.copy(content = it)) },
                                        onRemove = { viewModel.removeBlock(index) },
                                        onAttachMedia = { 
                                            activeBlockIndex = index
                                            imagePicker.launch()
                                        },
                                        focusRequester = focusRequesters[index]
                                    )
                                }
                                is ContentBlock.ImageGroup -> {
                                    ImageGroupBlockItem(
                                        urls = block.urls,
                                        onRemove = { viewModel.removeBlock(index) },
                                        onAddTextBelow = { viewModel.addHtmlBlock(afterIndex = index) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
