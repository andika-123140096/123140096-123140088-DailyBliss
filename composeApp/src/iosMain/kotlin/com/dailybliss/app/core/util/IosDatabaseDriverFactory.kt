package com.dailybliss.app.core.util

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.dailybliss.app.data.local.BlissDatabase

/**
 * iOS implementation of DatabaseDriverFactory
 *
 * Menggunakan NativeSqliteDriver yang membungkus SQLite native iOS.
 * Database disimpan di Documents directory aplikasi.
 */
class IosDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver = NativeSqliteDriver(
        schema = BlissDatabase.Schema,
        name = "bliss_v2.db",
    )
}
