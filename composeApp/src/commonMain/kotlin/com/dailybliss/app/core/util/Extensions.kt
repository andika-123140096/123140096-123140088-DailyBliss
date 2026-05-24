package com.dailybliss.app.core.util

import io.ktor.util.encodeBase64
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun ByteArray.toBase64(): String = this.encodeBase64()

val Instant.dateStr: String
    get() {
        val localDate = this.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return "${localDate.year}-${localDate.monthNumber.toString().padStart(2, '0')}-${localDate.dayOfMonth.toString().padStart(2, '0')}"
    }

/**
 * Format angka ke dalam format mata uang (ribuan dengan titik)
 */
fun Double.formatCurrency(): String {
    val integerPart = this.toLong().toString()
    return integerPart.reversed().chunked(3).joinToString(".").reversed()
}
