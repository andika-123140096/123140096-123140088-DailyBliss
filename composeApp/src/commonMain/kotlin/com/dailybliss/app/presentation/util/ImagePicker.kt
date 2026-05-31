package com.dailybliss.app.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher as peekabooRememberImagePickerLauncher

@Composable
fun rememberImagePickerLauncher(onResult: (List<ByteArray>) -> Unit): ImagePickerLauncher {
    val scope = rememberCoroutineScope()
    val launcher = peekabooRememberImagePickerLauncher(
        selectionMode = SelectionMode.Multiple(maxSelection = 5),
        scope = scope,
        onResult = onResult,
    )
    return object : ImagePickerLauncher {
        override fun launch() {
            launcher.launch()
        }
    }
}

interface ImagePickerLauncher {
    fun launch()
}

interface FileStorage {
    suspend fun saveImage(bytes: ByteArray): String?
    suspend fun loadImage(path: String): ByteArray?
}
