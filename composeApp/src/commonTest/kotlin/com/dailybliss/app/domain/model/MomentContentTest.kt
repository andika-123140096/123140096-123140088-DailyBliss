package com.dailybliss.app.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MomentContentTest {

    @Test
    fun `toHtml should convert html blocks correctly`() {
        val content = MomentContent(listOf(
            ContentBlock.Html("<p>Hello</p>"),
            ContentBlock.Html("<p>World</p>")
        ))
        val html = content.toHtml()
        assertEquals("<p>Hello</p>\n<p>World</p>", html)
    }

    @Test
    fun `toHtml should convert image groups correctly`() {
        val content = MomentContent(listOf(
            ContentBlock.Html("<p>Text before</p>"),
            ContentBlock.ImageGroup(listOf("url1", "url2")),
            ContentBlock.Html("<p>Text after</p>")
        ))
        val html = content.toHtml()
        assertTrue(html.contains("<div class=\"image-group\">"))
        assertTrue(html.contains("<img src=\"url1\" />"))
        assertTrue(html.contains("<img src=\"url2\" />"))
    }

    @Test
    fun `fromHtml should parse images into ImageGroup`() {
        val html = """
            <p>Intro</p>
            <div class="image-group">
                <img src="img1.jpg" />
                <img src="img2.jpg" />
            </div>
            <p>Outro</p>
        """.trimIndent()
        
        val content = MomentContent.fromHtml(html)
        
        assertEquals(3, content.blocks.size)
        assertTrue(content.blocks[0] is ContentBlock.Html)
        assertTrue(content.blocks[1] is ContentBlock.ImageGroup)
        assertTrue(content.blocks[2] is ContentBlock.Html)
        
        val imageGroup = content.blocks[1] as ContentBlock.ImageGroup
        assertEquals(2, imageGroup.urls.size)
        assertEquals("img1.jpg", imageGroup.urls[0])
        assertEquals("img2.jpg", imageGroup.urls[1])
    }

    @Test
    fun `fromHtml should handle plain text as Html block`() {
        val html = "Just plain text"
        val content = MomentContent.fromHtml(html)
        assertEquals(1, content.blocks.size)
        assertEquals("Just plain text", (content.blocks[0] as ContentBlock.Html).content)
    }

    @Test
    fun `fromHtml should handle multiple image groups`() {
        val html = """
            <div class="image-group"><img src="1.jpg" /></div>
            <p>Middle</p>
            <div class="image-group"><img src="2.jpg" /></div>
        """.trimIndent()
        
        val content = MomentContent.fromHtml(html)
        assertEquals(3, content.blocks.size)
        assertTrue(content.blocks[0] is ContentBlock.ImageGroup)
        assertTrue(content.blocks[1] is ContentBlock.Html)
        assertTrue(content.blocks[2] is ContentBlock.ImageGroup)
    }
}
