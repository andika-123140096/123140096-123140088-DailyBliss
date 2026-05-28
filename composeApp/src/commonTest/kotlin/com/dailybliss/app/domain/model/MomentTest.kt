package com.dailybliss.app.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MomentTest {
    @Test
    fun `preview should strip html tags`() {
        val moment = Moment(
            title = "Test",
            content = "<b>Bold</b> <i>Italic</i> <div class=\"image-group\"><img src=\"url\" /></div> Text"
        )
        
        val preview = moment.preview
        assertEquals("Bold Italic Text", preview)
    }

    @Test
    fun `preview should truncate long text`() {
        val longText = "A".repeat(200)
        val moment = Moment(title = "Test", content = longText)
        
        val preview = moment.preview
        assertTrue(preview.length <= 123) // 120 + "..."
        assertTrue(preview.endsWith("..."))
    }

    @Test
    fun `preview should be empty if content is empty`() {
        val moment = Moment(title = "Test", content = "")
        assertEquals("", moment.preview)
    }
}
