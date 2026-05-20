package com.dailybliss.app.presentation.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dailybliss.app.core.util.PlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@Composable
actual fun rememberImagePickerLauncher(onResult: (List<ByteArray>) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(),
            onResult = { uris ->
                if (uris.isNotEmpty()) {
                    try {
                        val bytesList =
                            uris.mapNotNull { uri ->
                                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            }
                        onResult(bytesList)
                    } catch (e: Exception) {
                        onResult(emptyList())
                    }
                } else {
                    onResult(emptyList())
                }
            },
        )

    return remember {
        object : ImagePickerLauncher {
            override fun launch() {
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }
}

actual class FileStorage actual constructor(private val context: PlatformContext) {
    actual suspend fun saveImage(bytes: ByteArray): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "moment_${UUID.randomUUID()}.jpg"
            val file = File(context.androidContext.filesDir, fileName)
            file.writeBytes(bytes)
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
