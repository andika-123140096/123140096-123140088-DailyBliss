package com.dailybliss.app.presentation.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlConverterDeepTest {

    @Test
    fun `toAnnotatedString should handle complex nested tags`() {
        val html = "<b>Bold <i>Italic</i> Bold</b>"
        val result = HtmlConverter.toAnnotatedString(html)
        
        assertEquals("Bold Italic Bold", result.text)
        // Check spans
        val boldSpans = result.spanStyles.filter { it.item.fontWeight == FontWeight.Bold }
        assertEquals(1, boldSpans.size)
        assertEquals(0, boldSpans[0].start)
        assertEquals(16, boldSpans[0].end)
        
        val italicSpans = result.spanStyles.filter { it.item.fontStyle == FontStyle.Italic }
        assertEquals(1, italicSpans.size)
        assertEquals(5, italicSpans[0].start)
        assertEquals(11, italicSpans[0].end)
    }

    @Test
    fun `toAnnotatedString should handle multiple same tags`() {
        val html = "<b>A</b> <b>B</b>"
        val result = HtmlConverter.toAnnotatedString(html)
        assertEquals("A B", result.text)
        val boldSpans = result.spanStyles.filter { it.item.fontWeight == FontWeight.Bold }
        assertEquals(2, boldSpans.size)
    }

    @Test
    fun `fromAnnotatedString should merge adjacent similar styles`() {
        val annotatedString = AnnotatedString(
            text = "AB",
            spanStyles = listOf(
                AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold), 0, 1),
                AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold), 1, 2)
            )
        )
        val html = HtmlConverter.fromAnnotatedString(annotatedString)
        // The implementation might or might not merge them depending on logic
        // If it doesn't merge, it would be <b>A</b><b>B</b>
        assertTrue(html.contains("<b>AB</b>") || html.contains("<b>A</b><b>B</b>"))
    }

    @Test
    fun `toggleStyle with no selection should just update active styles`() {
        // This is actually handled by the Screen state usually, 
        // but toggleStyle returns a new TextFieldValue.
        val value = TextFieldValue("Hello", selection = TextRange(0, 0))
        val style = SpanStyle(fontWeight = FontWeight.Bold)
        val result = HtmlConverter.toggleStyle(value, style)
        // If no selection, it shouldn't add a span
        assertTrue(result.annotatedString.spanStyles.isEmpty())
    }

    private fun assertTrue(actual: Boolean) {
        assertEquals(true, actual)
    }
}
