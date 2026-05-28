package com.dailybliss.app.presentation.util

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HtmlConverterInternalTest {

    @Test
    fun `isSimilarStyle should correctly compare bold`() {
        val s1 = SpanStyle(fontWeight = FontWeight.Bold)
        val s2 = SpanStyle(fontWeight = FontWeight.W700)
        assertTrue(HtmlConverter.isSimilarStyle(s1, s2))

        val s3 = SpanStyle(fontWeight = FontWeight.Normal)
        assertFalse(HtmlConverter.isSimilarStyle(s1, s3))
    }

    @Test
    fun `isSimilarStyle should correctly compare italic`() {
        val s1 = SpanStyle(fontStyle = FontStyle.Italic)
        val s2 = SpanStyle(fontStyle = FontStyle.Normal)
        assertFalse(HtmlConverter.isSimilarStyle(s1, s2))
    }

    @Test
    fun `isSimilarStyle should correctly compare underline`() {
        val s1 = SpanStyle(textDecoration = TextDecoration.Underline)
        val s2 = SpanStyle(textDecoration = TextDecoration.None)
        assertFalse(HtmlConverter.isSimilarStyle(s1, s2))
    }

    private fun assertTrue(actual: Boolean) {
        assertEquals(true, actual)
    }

    private fun assertFalse(actual: Boolean) {
        assertEquals(false, actual)
    }
}
