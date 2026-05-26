package com.dailybliss.app.presentation.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToURL
import platform.Foundation.NSURL
import platform.Foundation.UUID

class IosFileStorage : FileStorage {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun saveImage(bytes: ByteArray): String? {
        return try {
            val fileName = "moment_${UUID().UUIDString}.jpg"
            val fileManager = NSFileManager.defaultManager
            val documentsUrl = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).first() as NSURL
            val fileUrl = documentsUrl.URLByAppendingPathComponent(fileName)!!
            
            val data = bytes.usePinned {
                NSData.dataWithBytes(it.addressOf(0), bytes.size.toULong())
            }
            
            if (data.writeToURL(fileUrl, true)) {
                fileUrl.path
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun loadImage(path: String): ByteArray? {
        return try {
            val data = NSData.dataWithContentsOfFile(path) ?: return null
            ByteArray(data.length.toInt()).apply {
                usePinned {
                    platform.Foundation.memcpy(it.addressOf(0), data.bytes, data.length)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
