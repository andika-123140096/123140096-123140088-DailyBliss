package com.dailybliss.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dailybliss.app.presentation.util.HtmlConverter

@Composable
fun HtmlBlockItem(
    html: String,
    onHtmlChange: (String) -> Unit = { _ -> },
    activeStyles: Set<String> = emptySet(),
    onFocusValueChange: (TextFieldValue, Set<String>, (TextFieldValue) -> Unit) -> Unit = { _, _, _ -> },
    focusRequester: FocusRequester = remember { FocusRequester() },
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val parts = remember(html) { splitHtml(html) }

    Column(modifier = modifier.fillMaxWidth()) {
        parts.forEachIndexed { index, part ->
            when (part) {
                is HtmlPart.Text -> {
                    HtmlTextPart(
                        htmlPart = part,
                        onPartChange = { newTextHtml ->
                            val newParts = parts.toMutableList()
                            newParts[index] = HtmlPart.Text(newTextHtml)
                            onHtmlChange(joinParts(newParts))
                        },
                        parentActiveStyles = activeStyles,
                        onFocusValueChange = onFocusValueChange,
                        focusRequester = if (index == 0) focusRequester else remember { FocusRequester() },
                        isPlaceholderVisible = index == 0 && parts.size == 1,
                        enabled = enabled,
                    )
                }
                is HtmlPart.ImageGroup -> {
                    ImageGroupBlockItem(
                        urls = part.urls,
                        onRemove = {
                            if (enabled) {
                                val newParts = parts.toMutableList()
                                newParts.removeAt(index)
                                onHtmlChange(joinParts(newParts))
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HtmlTextPart(
    htmlPart: HtmlPart.Text,
    onPartChange: (String) -> Unit,
    parentActiveStyles: Set<String>,
    onFocusValueChange: (TextFieldValue, Set<String>, (TextFieldValue) -> Unit) -> Unit,
    focusRequester: FocusRequester,
    isPlaceholderVisible: Boolean,
    enabled: Boolean = true,
) {
    var isFocused by remember { mutableStateOf(false) }

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                annotatedString = HtmlConverter.toAnnotatedString(htmlPart.content),
                selection = TextRange(HtmlConverter.toAnnotatedString(htmlPart.content).length),
            ),
        )
    }

    LaunchedEffect(htmlPart.content) {
        val converted = HtmlConverter.toAnnotatedString(htmlPart.content)
        if (converted != textFieldValue.annotatedString) {
            textFieldValue = textFieldValue.copy(annotatedString = converted)
        }
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            if (!enabled) return@BasicTextField
            var finalValue = newValue

            // Text was added (typing)
            if (newValue.text.length > textFieldValue.text.length) {
                val diff = newValue.text.length - textFieldValue.text.length
                val cursorPosition = newValue.selection.start
                val startOfNewText = cursorPosition - diff

                finalValue = newValue.copy(
                    annotatedString = buildAnnotatedString {
                        append(newValue.text)

                        // Re-apply and extend previous styles
                        textFieldValue.annotatedString.spanStyles.forEach { span ->
                            var newStart = span.start
                            var newEnd = span.end

                            val isStyleActive = when {
                                span.item.fontWeight == FontWeight.Bold -> parentActiveStyles.contains("b")
                                span.item.fontStyle == FontStyle.Italic -> parentActiveStyles.contains("i")
                                span.item.textDecoration?.contains(TextDecoration.Underline) == true -> parentActiveStyles.contains("u")
                                else -> false
                            }

                            if (startOfNewText < span.start) {
                                newStart += diff
                                newEnd += diff
                            } else if (startOfNewText == span.start) {
                                if (isStyleActive) {
                                    newEnd += diff
                                } else {
                                    newStart += diff
                                    newEnd += diff
                                }
                            } else if (startOfNewText < span.end) {
                                newEnd += diff
                            } else if (startOfNewText == span.end) {
                                if (isStyleActive) {
                                    newEnd += diff
                                }
                            }

                            if (newStart < newEnd && newStart <= newValue.text.length) {
                                addStyle(span.item, newStart, minOf(newEnd, newValue.text.length))
                            }
                        }

                        // Apply NEW active styles from parent (toolbar) for any range not already covered
                        parentActiveStyles.forEach { styleKey ->
                            val style = when (styleKey) {
                                "b" -> SpanStyle(fontWeight = FontWeight.Bold)
                                "i" -> SpanStyle(fontStyle = FontStyle.Italic)
                                "u" -> SpanStyle(textDecoration = TextDecoration.Underline)
                                else -> null
                            }
                            style?.let {
                                // Only add if not already covered by existing (extended) style
                                val alreadyCovered = finalValue.annotatedString.spanStyles.any { s ->
                                    s.start <= startOfNewText &&
                                        s.end >= cursorPosition &&
                                        (
                                            (styleKey == "b" && s.item.fontWeight == FontWeight.Bold) ||
                                                (styleKey == "i" && s.item.fontStyle == FontStyle.Italic) ||
                                                (styleKey == "u" && s.item.textDecoration?.contains(TextDecoration.Underline) == true)
                                            )
                                }
                                if (!alreadyCovered) {
                                    addStyle(it, startOfNewText, cursorPosition)
                                }
                            }
                        }
                    },
                )
            }
            // Text was deleted
            else if (newValue.text.length < textFieldValue.text.length) {
                val diff = textFieldValue.text.length - newValue.text.length
                val cursorPosition = newValue.selection.start

                finalValue = newValue.copy(
                    annotatedString = buildAnnotatedString {
                        append(newValue.text)
                        textFieldValue.annotatedString.spanStyles.forEach { span ->
                            var newStart = span.start
                            var newEnd = span.end

                            if (cursorPosition <= span.start) {
                                newStart = maxOf(0, newStart - diff)
                                newEnd = maxOf(0, newEnd - diff)
                            } else if (cursorPosition < span.end) {
                                newEnd = maxOf(newStart, newEnd - diff)
                            }

                            if (newStart < newEnd && newStart < newValue.text.length) {
                                addStyle(span.item, newStart, minOf(newEnd, newValue.text.length))
                            }
                        }
                    },
                )
            } else {
                // Selection or composition change - keep spans from previous state
                finalValue = newValue.copy(annotatedString = textFieldValue.annotatedString)
            }

            textFieldValue = finalValue

            val newHtml = HtmlConverter.fromAnnotatedString(finalValue.annotatedString)
            if (newHtml != htmlPart.content) {
                onPartChange(newHtml)
            }

            if (isFocused) {
                val currentStyles = getActiveStylesAt(finalValue)
                onFocusValueChange(finalValue, currentStyles) { updatedFromParent ->
                    if (updatedFromParent.annotatedString != textFieldValue.annotatedString) {
                        textFieldValue = updatedFromParent
                        onPartChange(HtmlConverter.fromAnnotatedString(updatedFromParent.annotatedString))
                    }
                }
            }
        },
        readOnly = !enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .focusRequester(focusRequester)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onFocusValueChange(textFieldValue, getActiveStylesAt(textFieldValue)) { newValue ->
                        textFieldValue = newValue
                    }
                }
            },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.2.sp,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            if (isPlaceholderVisible && textFieldValue.text.isEmpty()) {
                Text(
                    text = "Mulai menulis...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                )
            }
            innerTextField()
        },
    )
}

private fun getActiveStylesAt(value: TextFieldValue): Set<String> {
    val selection = value.selection
    val spans = value.annotatedString.spanStyles
    val active = mutableSetOf<String>()

    // Check style at cursor or within selection
    val checkPos = if (selection.collapsed) maxOf(0, selection.start - 1) else selection.start

    spans.filter {
        if (selection.collapsed) {
            it.start <= checkPos && it.end > checkPos
        } else {
            it.start < selection.max && it.end > selection.min
        }
    }.forEach { span ->
        when {
            span.item.fontWeight == FontWeight.Bold -> active.add("b")
            span.item.fontStyle == FontStyle.Italic -> active.add("i")
            span.item.textDecoration?.contains(TextDecoration.Underline) == true -> active.add("u")
        }
    }
    return active
}

private sealed class HtmlPart {
    data class Text(val content: String) : HtmlPart()
    data class ImageGroup(val urls: List<String>) : HtmlPart()
}

private fun splitHtml(html: String): List<HtmlPart> {
    if (html.isEmpty()) return listOf(HtmlPart.Text(""))

    val parts = mutableListOf<HtmlPart>()
    val imageGroupRegex = Regex("<div class=\"image-group\">(.*?)</div>", RegexOption.DOT_MATCHES_ALL)

    var lastIndex = 0
    imageGroupRegex.findAll(html).forEach { match ->
        val textBefore = html.substring(lastIndex, match.range.first)
        parts.add(HtmlPart.Text(textBefore))

        val groupContent = match.groupValues[1]
        val urls = Regex("<img src=\"(.*?)\" />").findAll(groupContent)
            .map { it.groupValues[1] }.toList()
        parts.add(HtmlPart.ImageGroup(urls))

        lastIndex = match.range.last + 1
    }

    val textAfter = html.substring(lastIndex)
    parts.add(HtmlPart.Text(textAfter))

    return parts
}

private fun joinParts(parts: List<HtmlPart>): String = parts.joinToString("") { part ->
    when (part) {
        is HtmlPart.Text -> part.content
        is HtmlPart.ImageGroup -> {
            "<div class=\"image-group\">" + part.urls.joinToString("") { "<img src=\"$it\" />" } + "</div>"
        }
    }
}

@Composable
fun ImageGroupBlockItem(
    urls: List<String>,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { urls.size })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 8.dp,
        ) { page ->
            AsyncImage(
                model = urls[page],
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        if (urls.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    "${pagerState.currentPage + 1} / ${urls.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                .size(24.dp),
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
fun FormattingToolbar(
    activeStyles: Set<String>,
    onStyleClick: (String) -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .height(40.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            StyleToggleButton(activeStyles.contains("b"), Icons.Default.FormatBold, "Bold") { onStyleClick("b") }
            StyleToggleButton(activeStyles.contains("i"), Icons.Default.FormatItalic, "Italic") { onStyleClick("i") }
            StyleToggleButton(activeStyles.contains("u"), Icons.Default.FormatUnderlined, "Underline") { onStyleClick("u") }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onGalleryClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    Icons.Default.Collections,
                    "Gallery",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun StyleToggleButton(
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        colors = if (isActive) {
            IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            )
        } else {
            IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    ) {
        Icon(icon, contentDescription, modifier = Modifier.size(20.dp))
    }
}
