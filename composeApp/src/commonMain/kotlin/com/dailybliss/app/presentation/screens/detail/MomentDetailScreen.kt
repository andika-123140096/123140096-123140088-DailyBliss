package com.dailybliss.app.presentation.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailybliss.app.presentation.components.FormattingToolbar
import com.dailybliss.app.presentation.components.HtmlBlockItem
import com.dailybliss.app.presentation.components.LoadingIndicator
import com.dailybliss.app.presentation.util.HtmlConverter
import com.dailybliss.app.presentation.util.rememberImagePickerLauncher
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

    MomentDetailScreenContent(
        uiState = uiState,
        onTitleChange = viewModel::updateTitle,
        onContentChange = viewModel::updateContent,
        onAddImage = viewModel::addImage,
        onSaveChanges = viewModel::saveChanges,
        onDeleteMoment = { showDeleteDialog = true },
        onConfirmDelete = {
            viewModel.deleteMoment { onNavigateBack() }
        },
        onNavigateBack = onNavigateBack,
        showDeleteDialog = showDeleteDialog,
        onDismissDeleteDialog = { showDeleteDialog = false },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MomentDetailScreenContent(
    uiState: MomentDetailUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onAddImage: (List<ByteArray>, Int) -> Unit,
    onSaveChanges: () -> Unit,
    onDeleteMoment: () -> Unit,
    onConfirmDelete: () -> Unit,
    onNavigateBack: () -> Unit,
    showDeleteDialog: Boolean,
    onDismissDeleteDialog: () -> Unit,
) {
    var focusedValue by remember { mutableStateOf<TextFieldValue?>(null) }
    var updateFocusedValue by remember { mutableStateOf<((TextFieldValue) -> Unit)?>(null) }
    var activeStyles by remember { mutableStateOf(setOf<String>()) }
    var currentBlockOffset by remember { mutableStateOf(0) }
    var lastCursorPosition by remember { mutableStateOf(-1) }

    val imagePicker = rememberImagePickerLauncher(
        onResult = { bytesList ->
            if (bytesList.isNotEmpty()) {
                onAddImage(bytesList, lastCursorPosition)
            }
        },
    )

    Scaffold(
        modifier = Modifier.testTag("MOMENT_DETAIL_SCREEN"),
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
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("BACK_BUTTON")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (uiState.isDirty) {
                        TextButton(
                            onClick = onSaveChanges,
                            enabled = !uiState.isSaving,
                            modifier = Modifier.testTag("SAVE_BUTTON"),
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp).testTag("SAVING_INDICATOR"),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text(
                                    "Simpan",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                )
                            }
                        }
                    } else {
                        TextButton(
                            onClick = onDeleteMoment,
                            modifier = Modifier.testTag("DELETE_BUTTON"),
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        ) {
                            Text(
                                "Hapus",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.testTag("LOADING_INDICATOR")) {
                LoadingIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize().testTag("ERROR_CONTAINER"),
                contentAlignment = Alignment.Center,
            ) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            uiState.moment?.let { moment ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .imePadding(),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            BasicTextField(
                                value = moment.title,
                                onValueChange = onTitleChange,
                                textStyle = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 24.sp,
                                    lineHeight = 30.sp,
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    if (moment.title.isEmpty()) {
                                        Text(
                                            text = "Judul Cerita",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                fontWeight = FontWeight.Bold,
                                            ),
                                        )
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .testTag("TITLE_TEXT_FIELD"),
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val dateText = moment.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date.let {
                                "${it.dayOfMonth} ${it.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${it.year}"
                            }
                            Text(
                                text = dateText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Mood & Tags
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            ) {
                                moment.mood?.let { mood ->
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(mood) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("MOOD_CHIP"),
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            labelColor = MaterialTheme.colorScheme.primary,
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        ),
                                        border = null,
                                    )
                                }

                                if (moment.tags.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
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
                                                modifier = Modifier.testTag("TAG_CHIP_$tag"),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.padding(horizontal = 0.dp).testTag("HTML_CONTENT_BOX")) {
                            HtmlBlockItem(
                                html = moment.content,
                                onHtmlChange = onContentChange,
                                activeStyles = activeStyles,
                                onFocusValueChange = { value, styles, update, offset ->
                                    focusedValue = value
                                    activeStyles = styles
                                    updateFocusedValue = update
                                    currentBlockOffset = offset
                                },
                                enabled = true,
                            )
                        }

                        Spacer(modifier = Modifier.height(100.dp))
                    }

                    if (focusedValue != null) {
                        FormattingToolbar(
                            activeStyles = activeStyles,
                            onStyleClick = { style ->
                                activeStyles = if (activeStyles.contains(style)) {
                                    activeStyles - style
                                } else {
                                    activeStyles + style
                                }

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
                                lastCursorPosition = if (focusedValue != null) {
                                    currentBlockOffset + focusedValue!!.selection.start
                                } else {
                                    -1
                                }
                                imagePicker.launch()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("FORMATTING_TOOLBAR"),
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = { Text("Hapus Jurnal") },
            text = { Text("Apakah kamu yakin ingin menghapus jurnal ini? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDelete,
                    modifier = Modifier.testTag("CONFIRM_DELETE_BUTTON"),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialog, modifier = Modifier.testTag("CANCEL_DELETE_BUTTON")) {
                    Text("Batal")
                }
            },
        )
    }
}
