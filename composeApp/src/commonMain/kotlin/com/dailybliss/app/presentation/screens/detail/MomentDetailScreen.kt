package com.dailybliss.app.presentation.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailybliss.app.presentation.components.*
import com.dailybliss.app.presentation.util.HtmlConverter
import com.dailybliss.app.presentation.util.rememberImagePickerLauncher
import org.koin.compose.viewmodel.koinViewModel

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MomentDetailScreen(
    momentId: Long,
    onNavigateBack: () -> Unit,
    viewModel: MomentDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var focusedValue by remember { mutableStateOf<TextFieldValue?>(null) }
    var updateFocusedValue by remember { mutableStateOf<((TextFieldValue) -> Unit)?>(null) }
    var activeStyles by remember { mutableStateOf(setOf<String>()) }
    
    var lastCursorPosition by remember { mutableStateOf(-1) }
    val imagePicker = rememberImagePickerLauncher(
        onResult = { bytesList ->
            if (bytesList.isNotEmpty()) {
                viewModel.addImage(bytesList, lastCursorPosition)
            }
        }
    )
    
    LaunchedEffect(momentId) {
        viewModel.loadMoment(momentId)
    }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MomentDetailEvent.MomentDeleted -> onNavigateBack()
                else -> {}
            }
        }
    }
    
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteMoment()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
    
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            if (focusedValue != null) {
                FormattingToolbar(
                    activeStyles = activeStyles,
                    onStyleClick = { style ->
                        activeStyles = if (activeStyles.contains(style)) {
                            activeStyles - style
                        } else {
                            activeStyles + style
                        }
                        
                        // Also apply to selection if exists
                        focusedValue?.let { value ->
                            if (!value.selection.collapsed) {
                                val spanStyle = when (style) {
                                    "b" -> SpanStyle(fontWeight = FontWeight.Bold)
                                    "i" -> SpanStyle(fontStyle = FontStyle.Italic)
                                    "u" -> SpanStyle(textDecoration = TextDecoration.Underline)
                                    else -> SpanStyle()
                                }
                                val newValue = HtmlConverter.toggleStyle(value, spanStyle)
                                updateFocusedValue?.invoke(newValue)
                            }
                        }
                    },
                    onGalleryClick = { 
                        lastCursorPosition = focusedValue?.selection?.start ?: -1
                        imagePicker.launch()
                    },
                    modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.ime)
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is MomentDetailUiState.Loading -> LoadingIndicator()
            is MomentDetailUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 120.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Gray)
                        }
                        
                        BasicTextField(
                            value = state.title,
                            onValueChange = viewModel::onTitleChange,
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                lineHeight = 30.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                if (state.title.isEmpty()) {
                                    Text(
                                        text = "Judul...",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.LightGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                        
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(Icons.Outlined.Delete, "Delete", tint = Color.Gray)
                        }
                    }

                    Box(modifier = Modifier.padding(horizontal = 0.dp)) {
                        HtmlBlockItem(
                            html = state.content,
                            onHtmlChange = viewModel::onContentChange,
                            activeStyles = activeStyles,
                            onFocusValueChange = { value, update ->
                                focusedValue = value
                                updateFocusedValue = update
                            },
                            focusRequester = remember { FocusRequester() }
                        )
                    }
                }
            }
            is MomentDetailUiState.NotFound -> {
                EmptyState("Tidak Ditemukan", "Momen mungkin telah dihapus.")
            }
        }
    }
}


@Composable
private fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus Momen?") },
        text = { Text("Tindakan ini tidak dapat dibatalkan.") },
        confirmButton = {
            TextButton(onClick = onConfirm, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text("HAPUS")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)) {
                Text("BATAL")
            }
        },
        containerColor = Color.White
    )
}
