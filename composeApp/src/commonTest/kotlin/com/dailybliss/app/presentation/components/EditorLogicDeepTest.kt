package com.dailybliss.app.presentation.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EditorLogicDeepTest {

    @Test
    fun `splitHtml with multiple nested image groups and text`() {
        val html = "T1<div class=\"image-group\"><img src=\"1\" /></div>T2<div class=\"image-group\"><img src=\"2\" /><img src=\"3\" /></div>T3"
        val parts = splitHtml(html)
        
        assertEquals(5, parts.size)
        assertEquals("T1", (parts[0] as HtmlPart.Text).content)
        assertEquals(listOf("1"), (parts[1] as HtmlPart.ImageGroup).urls)
        assertEquals("T2", (parts[2] as HtmlPart.Text).content)
        assertEquals(listOf("2", "3"), (parts[3] as HtmlPart.ImageGroup).urls)
        assertEquals("T3", (parts[4] as HtmlPart.Text).content)
    }

    @Test
    fun `joinParts should be inverse of splitHtml`() {
        val original = "Hello <div class=\"image-group\"><img src=\"a.png\" /></div> World"
        val parts = splitHtml(original)
        val result = joinParts(parts)
        assertEquals(original, result)
    }

    @Test
    fun `calculateTextOffsets for mixed content`() {
        val parts = listOf(
            HtmlPart.Text("123"),
            HtmlPart.ImageGroup(listOf("url")),
            HtmlPart.Text("45"),
            HtmlPart.ImageGroup(listOf("url2", "url3")),
            HtmlPart.Text("6")
        )
        val offsets = calculateTextOffsets(parts)
        // Offsets: 
        // 0: start of "123"
        // 3: end of "123", start of group1 (placeholder len 1)
        // 4: end of group1, start of "45"
        // 6: end of "45", start of group2
        // 7: end of group2, start of "6"
        assertEquals(listOf(0, 3, 4, 6, 7), offsets)
    }

    @Test
    fun `splitHtml with leading and trailing image groups`() {
        val html = "<div class=\"image-group\"><img src=\"1\" /></div>Text<div class=\"image-group\"><img src=\"2\" /></div>"
        val parts = splitHtml(html)
        // Should produce: ["", Group1, "Text", Group2, ""]
        assertEquals(5, parts.size)
        assertTrue(parts[0] is HtmlPart.Text && (parts[0] as HtmlPart.Text).content.isEmpty())
        assertTrue(parts[4] is HtmlPart.Text && (parts[4] as HtmlPart.Text).content.isEmpty())
    }
}
