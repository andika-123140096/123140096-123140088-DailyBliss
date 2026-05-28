package com.dailybliss.app.presentation.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EditorUtilsTest {

    @Test
    fun `splitHtml should separate text and image groups`() {
        val html = "Text before<div class=\"image-group\"><img src=\"url1\" /><img src=\"url2\" /></div>Text after"
        val parts = splitHtml(html)
        
        assertEquals(3, parts.size)
        assertTrue(parts[0] is HtmlPart.Text)
        assertEquals("Text before", (parts[0] as HtmlPart.Text).content)
        
        assertTrue(parts[1] is HtmlPart.ImageGroup)
        assertEquals(listOf("url1", "url2"), (parts[1] as HtmlPart.ImageGroup).urls)
        
        assertTrue(parts[2] is HtmlPart.Text)
        assertEquals("Text after", (parts[2] as HtmlPart.Text).content)
    }

    @Test
    fun `joinParts should reconstruct html correctly`() {
        val parts = listOf(
            HtmlPart.Text("Start "),
            HtmlPart.ImageGroup(listOf("img1.png")),
            HtmlPart.Text(" End")
        )
        
        val html = joinParts(parts)
        assertEquals("Start <div class=\"image-group\"><img src=\"img1.png\" /></div> End", html)
    }

    @Test
    fun `calculateTextOffsets should return correct positions`() {
        val parts = listOf(
            HtmlPart.Text("ABC"),
            HtmlPart.ImageGroup(listOf("url")),
            HtmlPart.Text("D")
        )
        val offsets = calculateTextOffsets(parts)
        assertEquals(listOf(0, 3, 4), offsets)
    }

    @Test
    fun `splitHtml with multiple image groups`() {
        val html = "A<div class=\"image-group\"><img src=\"1\" /></div>B<div class=\"image-group\"><img src=\"2\" /></div>C"
        val parts = splitHtml(html)
        assertEquals(5, parts.size)
        assertTrue(parts[1] is HtmlPart.ImageGroup)
        assertTrue(parts[3] is HtmlPart.ImageGroup)
    }
}
