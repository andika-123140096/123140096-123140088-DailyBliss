package com.dailybliss.app.core.util

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.dailybliss.app.data.local.BlissDatabase

/**
 * Android implementation of DatabaseDriverFactory
 *
 * Menggunakan AndroidSqliteDriver yang membungkus SQLite bawaan Android.
 * Database disimpan di internal storage aplikasi.
 */
class AndroidDatabaseDriverFactory(private val context: PlatformContext) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver = AndroidSqliteDriver(
        schema = BlissDatabase.Schema,
        context = (context as AndroidPlatformContext).androidContext,
        name = "bliss.db",
    )
}
