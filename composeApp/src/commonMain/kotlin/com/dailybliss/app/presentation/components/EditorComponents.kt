package com.dailybliss.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun HtmlBlockItem(
    html: String,
    onHtmlChange: (String) -> Unit,
    onRemove: () -> Unit,
    onAttachMedia: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(html, TextRange(html.length))) }

    // Update internal state when external html changes (e.g. from load)
    LaunchedEffect(html) {
        if (html != textFieldValue.text) {
            textFieldValue = TextFieldValue(html, TextRange(html.length))
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Media Action Button - Top Aligned
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isFocused || html.isEmpty()) {
                IconButton(
                    onClick = onAttachMedia,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Attach Image",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))

        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onHtmlChange(it.text)
            },
            modifier = Modifier
                .weight(1f)
                .padding(top = 2.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { 
                    isFocused = it.isFocused 
                }
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown && 
                        keyEvent.key == Key.Backspace && 
                        html.isEmpty()) {
                        onRemove()
                        true
                    } else {
                        false
                    }
                },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 28.sp,
                letterSpacing = 0.2.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                if (html.isEmpty()) {
                    Text(
                        text = "Mulai menulis...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                }
                innerTextField()
            }
        )

        // Remove Action Button - Top Aligned
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isFocused && html.isNotEmpty()) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ImageGroupBlockItem(
    urls: List<String>,
    onRemove: () -> Unit,
    onAddTextBelow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { urls.size })

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                pageSpacing = 8.dp
            ) { page ->
                AsyncImage(
                    model = urls[page],
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp),
                    contentScale = ContentScale.FillWidth
                )
            }
            
            // Image count indicator
            if (urls.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${pagerState.currentPage + 1} / ${urls.size}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .size(28.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        // Add text button below image group
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                .clickable { onAddTextBelow() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Add,
                    null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Tambah tulisan di bawah gambar",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
