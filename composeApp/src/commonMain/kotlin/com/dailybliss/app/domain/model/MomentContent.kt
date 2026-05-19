package com.dailybliss.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class ContentBlock {
    @Serializable
    data class Html(val content: String) : ContentBlock()
    
    @Serializable
    data class ImageGroup(val urls: List<String>) : ContentBlock()
}

@Serializable
data class MomentContent(
    val blocks: List<ContentBlock> = listOf(ContentBlock.Html(""))
) {
    fun toHtml(): String {
        return blocks.joinToString("\n") { block ->
            when (block) {
                is ContentBlock.Html -> block.content
                is ContentBlock.ImageGroup -> {
                    val images = block.urls.joinToString("") { "<img src=\"$it\" />" }
                    "<div class=\"image-group\">$images</div>"
                }
            }
        }
    }

    companion object {
        fun fromHtml(html: String): MomentContent {
            if (html.isBlank()) return MomentContent(listOf(ContentBlock.Html("")))
            
            val blocks = mutableListOf<ContentBlock>()
            val imageGroupRegex = Regex("<div class=\"image-group\">(.*?)</div>", RegexOption.DOT_MATCHES_ALL)
            val imgRegex = Regex("<img src=\"(.*?)\" />")
            
            var lastIndex = 0
            imageGroupRegex.findAll(html).forEach { match ->
                // Add text before image group
                val beforeText = html.substring(lastIndex, match.range.first).trim()
                if (beforeText.isNotEmpty()) {
                    blocks.add(ContentBlock.Html(beforeText))
                }
                
                // Extract images from group
                val groupContent = match.groupValues[1]
                val urls = imgRegex.findAll(groupContent).map { it.groupValues[1] }.toList()
                if (urls.isNotEmpty()) {
                    blocks.add(ContentBlock.ImageGroup(urls))
                }
                
                lastIndex = match.range.last + 1
            }
            
            // Add remaining text
            val remainingText = html.substring(lastIndex).trim()
            if (remainingText.isNotEmpty() || blocks.isEmpty()) {
                blocks.add(ContentBlock.Html(remainingText))
            }
            
            return MomentContent(blocks)
        }
    }
}
