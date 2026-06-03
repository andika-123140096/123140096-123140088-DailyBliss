package com.dailybliss.app.presentation.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.UUID
import platform.Foundation.dataWithBytes
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToURL

class IosFileStorage : FileStorage {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun saveImage(bytes: ByteArray): String? = try {
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

    override suspend fun loadImage(path: String): ByteArray? {
        return try {
            val fileManager = NSFileManager.defaultManager
            val actualPath = if (path.startsWith("/")) {
                path
            } else {
                val documentsUrl = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).first() as NSURL
                documentsUrl.URLByAppendingPathComponent(path)?.path ?: path
            }

            val data = NSData.dataWithContentsOfFile(actualPath) ?: return null
            ByteArray(data.length.toInt()).apply {
                usePinned {
                    platform.Foundation.memcpy(it.addressOf(0), data.bytes, data.length)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun saveFile(bytes: ByteArray, fileName: String): String? = try {
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

    override suspend fun loadFile(path: String): ByteArray? = loadImage(path)
}
