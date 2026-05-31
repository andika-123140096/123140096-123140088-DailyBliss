package com.dailybliss.app.presentation.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EditorComponentsLogicTest {

    @Test
    fun `splitHtml should split text and image groups correctly`() {
        val html = "Text before <div class=\"image-group\"><img src=\"url1\" /><img src=\"url2\" /></div> Text after"
        val parts = splitHtml(html)

        assertEquals(3, parts.size)
        assertTrue(parts[0] is HtmlPart.Text)
        assertEquals("Text before ", (parts[0] as HtmlPart.Text).content)

        assertTrue(parts[1] is HtmlPart.ImageGroup)
        assertEquals(listOf("url1", "url2"), (parts[1] as HtmlPart.ImageGroup).urls)

        assertTrue(parts[2] is HtmlPart.Text)
        assertEquals(" Text after", (parts[2] as HtmlPart.Text).content)
    }

    @Test
    fun `splitHtml should handle empty string`() {
        val parts = splitHtml("")
        assertEquals(1, parts.size)
        assertTrue(parts[0] is HtmlPart.Text)
        assertEquals("", (parts[0] as HtmlPart.Text).content)
    }

    @Test
    fun `splitHtml should handle only image group`() {
        val html = "<div class=\"image-group\"><img src=\"url1\" /></div>"
        val parts = splitHtml(html)

        // splitHtml implementation adds empty text parts around matches if not careful
        // current implementation:
        // parts.add(HtmlPart.Text(textBefore)) -> ""
        // parts.add(HtmlPart.ImageGroup(urls)) -> ImageGroup
        // lastIndex = match.range.last + 1
        // parts.add(HtmlPart.Text(textAfter)) -> ""

        assertTrue(parts.any { it is HtmlPart.ImageGroup })
        val imageGroup = parts.find { it is HtmlPart.ImageGroup } as HtmlPart.ImageGroup
        assertEquals(listOf("url1"), imageGroup.urls)
    }

    @Test
    fun `joinParts should reconstruct HTML correctly`() {
        val parts = listOf(
            HtmlPart.Text("Start "),
            HtmlPart.ImageGroup(listOf("img1.png")),
            HtmlPart.Text(" End"),
        )
        val html = joinParts(parts)
        assertEquals("Start <div class=\"image-group\"><img src=\"img1.png\" /></div> End", html)
    }

    @Test
    fun `splitHtml and joinParts should be reversible`() {
        val originalHtml = "<p>Hello</p><div class=\"image-group\"><img src=\"1.jpg\" /></div><p>World</p>"
        val parts = splitHtml(originalHtml)
        val joinedHtml = joinParts(parts)
        assertEquals(originalHtml, joinedHtml)
    }
}
