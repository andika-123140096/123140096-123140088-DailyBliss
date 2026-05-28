package com.dailybliss.app.core.util

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class ExtensionsTest {
    @Test
    fun `Instant dateStr should format correctly`() {
        val instant = Instant.fromEpochMilliseconds(1716681600000L) // 2024-05-26
        // Result depends on system timezone, so we might just check the format
        val regex = Regex("\\d{4}-\\d{2}-\\d{2}")
        assertTrue(regex.matches(instant.dateStr))
    }

    @Test
    fun `Double formatCurrency should format correctly`() {
        val value = 15000.0
        assertEquals("15.000", value.formatCurrency())

        val value2 = 1234567.0
        assertEquals("1.234.567", value2.formatCurrency())
    }

    private fun assertTrue(actual: Boolean) {
        assertEquals(true, actual)
    }
}
