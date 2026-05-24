package com.dailybliss.app.presentation.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.dailybliss.app.core.util.AndroidPlatformContext
import com.dailybliss.app.core.util.PlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@Composable
actual fun rememberImagePickerLauncher(onResult: (List<ByteArray>) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(),
            onResult = { uris ->
                if (uris.isNotEmpty()) {
                    scope.launch {
                        try {
                            val bytesList = withContext(Dispatchers.IO) {
                                uris.mapNotNull { uri ->
                                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                }
                            }
                            onResult(bytesList)
                        } catch (ignore: Exception) {
                            onResult(emptyList())
                        }
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

class AndroidFileStorage(private val context: PlatformContext) : FileStorage {
    private val androidContext = (context as AndroidPlatformContext).androidContext

    override suspend fun saveImage(bytes: ByteArray): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "moment_${UUID.randomUUID()}.jpg"
            val file = File(androidContext.filesDir, fileName)
            file.writeBytes(bytes)
            file.absolutePath
        } catch (ignore: Exception) {
            null
        }
    }

    override suspend fun loadImage(path: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            File(path).readBytes()
        } catch (ignore: Exception) {
            null
        }
    }
}
