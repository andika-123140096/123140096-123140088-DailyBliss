package com.dailybliss.app.presentation.screens.addnote

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
import androidx.compose.ui.focus.FocusRequester
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
import com.dailybliss.app.presentation.components.*
import com.dailybliss.app.presentation.util.HtmlConverter
import com.dailybliss.app.presentation.util.rememberImagePickerLauncher
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateMomentScreen(onNavigateBack: () -> Unit, viewModel: CreateMomentViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateMomentEvent.MomentSaved -> onNavigateBack()
                is CreateMomentEvent.Error -> snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    CreateMomentScreenContent(
        uiState = uiState,
        onTitleChange = viewModel::onTitleChange,
        onContentChange = viewModel::onContentChange,
        onAddImage = viewModel::addImage,
        onSaveMoment = viewModel::saveMoment,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateMomentScreenContent(
    uiState: CreateMomentUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onAddImage: (List<ByteArray>, Int) -> Unit,
    onSaveMoment: () -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("CREATE_MOMENT_SCREEN")
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.testTag("LOADING_INDICATOR")) {
                LoadingIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().imePadding()) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("BACK_BUTTON"),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp)
                                .testTag("SAVING_INDICATOR"),
                            strokeWidth = 2.dp,
                        )
                    }

                    TextButton(
                        onClick = onSaveMoment,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.testTag("SAVE_BUTTON"),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text(
                            "Simpan",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                            ),
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        BasicTextField(
                            value = uiState.title,
                            onValueChange = onTitleChange,
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                lineHeight = 30.sp,
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                if (uiState.title.isEmpty()) {
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

                        // Mood & Tags Display
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            uiState.mood?.let { mood ->
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
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                uiState.tags.forEach { tag ->
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

                    Box(modifier = Modifier.padding(horizontal = 0.dp).testTag("HTML_CONTENT_BOX")) {
                        HtmlBlockItem(
                            html = uiState.content,
                            onHtmlChange = onContentChange,
                            activeStyles = activeStyles,
                            onFocusValueChange = { value, styles, update, offset ->
                                focusedValue = value
                                activeStyles = styles
                                updateFocusedValue = update
                                currentBlockOffset = offset
                            },
                            focusRequester = remember { FocusRequester() },
                        )
                    }

                    // Extra padding for the last item to not be covered by toolbar
                    Spacer(modifier = Modifier.height(100.dp))
                }

                // Toolbar and Keyboard Handling - AIAssistant Mechanism
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp).testTag("SNACKBAR_HOST"),
        )
    }
}
