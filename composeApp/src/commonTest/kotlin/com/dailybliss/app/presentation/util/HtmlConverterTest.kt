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

class HtmlConverterTest {

    @Test
    fun `toAnnotatedString should parse basic tags`() {
        val html = "<b>Bold</b> <i>Italic</i> <u>Underline</u>"
        val annotatedString = HtmlConverter.toAnnotatedString(html)
        
        assertEquals("Bold Italic Underline", annotatedString.text)
        
        val spans = annotatedString.spanStyles
        assertTrue(spans.any { it.item.fontWeight == FontWeight.Bold })
        assertTrue(spans.any { it.item.fontStyle == FontStyle.Italic })
        assertTrue(spans.any { it.item.textDecoration == TextDecoration.Underline })
    }

    @Test
    fun `toAnnotatedString should handle strong and em`() {
        val html = "<strong>Strong</strong> <em>Em</em>"
        val annotatedString = HtmlConverter.toAnnotatedString(html)
        
        assertEquals("Strong Em", annotatedString.text)
        assertTrue(annotatedString.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
        assertTrue(annotatedString.spanStyles.any { it.item.fontStyle == FontStyle.Italic })
    }

    @Test
    fun `toAnnotatedString should handle line breaks`() {
        val html = "Line 1<br>Line 2<br />Line 3"
        val annotatedString = HtmlConverter.toAnnotatedString(html)
        assertEquals("Line 1\nLine 2\nLine 3", annotatedString.text)
    }

    @Test
    fun `toAnnotatedString should handle unclosed tags`() {
        val html = "<b>Unclosed"
        val annotatedString = HtmlConverter.toAnnotatedString(html)
        assertEquals("Unclosed", annotatedString.text)
        assertTrue(annotatedString.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
    }

    @Test
    fun `fromAnnotatedString should generate correct html`() {
        val text = "Bold Italic"
        val annotatedString = AnnotatedString(
            text = text,
            spanStyles = listOf(
                AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold), 0, 4),
                AnnotatedString.Range(SpanStyle(fontStyle = FontStyle.Italic), 5, 11)
            )
        )
        
        val html = HtmlConverter.fromAnnotatedString(annotatedString)
        assertEquals("<b>Bold</b> <i>Italic</i>", html)
    }

    @Test
    fun `fromAnnotatedString should escape special characters`() {
        val annotatedString = AnnotatedString("A < B & C > D\nNext")
        val html = HtmlConverter.fromAnnotatedString(annotatedString)
        assertEquals("A &lt; B &amp; C &gt; D<br/>Next", html)
    }

    @Test
    fun `toggleStyle should add style if not present`() {
        val value = TextFieldValue("Hello World", selection = TextRange(0, 5))
        val style = SpanStyle(fontWeight = FontWeight.Bold)
        
        val newValue = HtmlConverter.toggleStyle(value, style)
        assertTrue(newValue.annotatedString.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
    }

    @Test
    fun `toggleStyle should remove style if already present`() {
        val annotatedString = AnnotatedString(
            "Hello World",
            spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold), 0, 5))
        )
        val value = TextFieldValue(annotatedString, selection = TextRange(0, 5))
        val style = SpanStyle(fontWeight = FontWeight.Bold)
        
        val newValue = HtmlConverter.toggleStyle(value, style)
        assertTrue(newValue.annotatedString.spanStyles.isEmpty())
    }

    @Test
    fun `toggleStyle should handle partial overlap removal`() {
        val annotatedString = AnnotatedString(
            "Hello World",
            spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold), 0, 11))
        )
        val value = TextFieldValue(annotatedString, selection = TextRange(0, 5))
        val style = SpanStyle(fontWeight = FontWeight.Bold)
        
        val newValue = HtmlConverter.toggleStyle(value, style)
        val spans = newValue.annotatedString.spanStyles
        assertEquals(1, spans.size)
        assertEquals(5, spans[0].start)
        assertEquals(11, spans[0].end)
    }

    private fun assertTrue(actual: Boolean) {
        assertEquals(true, actual)
    }
}
