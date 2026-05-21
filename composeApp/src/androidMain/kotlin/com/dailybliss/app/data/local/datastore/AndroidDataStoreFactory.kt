package com.dailybliss.app.data.local.datastore

import com.dailybliss.app.core.util.AndroidPlatformContext
import com.dailybliss.app.core.util.PlatformContext

/**
 * Android implementation of [DataStoreFactory].
 *
 * Menyimpan file preferences di internal storage aplikasi
 * (`/data/data/<package>/files/`).
 */
class AndroidDataStoreFactory(private val context: PlatformContext) : DataStoreFactory {
    override fun producePath(): String = (context as AndroidPlatformContext).androidContext.filesDir.absolutePath
}
