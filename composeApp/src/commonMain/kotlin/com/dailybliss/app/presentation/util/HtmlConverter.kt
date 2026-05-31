package com.dailybliss.app.presentation.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration

object HtmlConverter {
    fun toAnnotatedString(html: String): AnnotatedString {
        if (html.isEmpty()) return AnnotatedString("")

        return buildAnnotatedString {
            var i = 0
            val tags = mutableListOf<Pair<String, Int>>() // tagName to start index

            // Clean HTML of common artifacts before parsing
            val cleanHtml =
                html
                    .replace("<br>", "\n")
                    .replace("<br/>", "\n")
                    .replace("<br />", "\n")

            while (i < cleanHtml.length) {
                if (cleanHtml[i] == '<') {
                    val closingBracket = cleanHtml.indexOf('>', i)
                    if (closingBracket != -1) {
                        val fullTag = cleanHtml.substring(i + 1, closingBracket)
                        val isClosing = fullTag.startsWith("/")
                        val tagName =
                            if (isClosing) {
                                fullTag
                                    .substring(
                                        1,
                                    ).lowercase()
                            } else {
                                fullTag.split(" ")[0].lowercase()
                            }

                        if (isClosing) {
                            val lastTagIndex = tags.indexOfLast { it.first == tagName }
                            if (lastTagIndex != -1) {
                                val tag = tags[lastTagIndex]
                                getStyleForTag(tagName)?.let { style ->
                                    addStyle(style, tag.second, length)
                                }
                                tags.removeAt(lastTagIndex)
                            }
                        } else {
                            tags.add(tagName to length)
                        }
                        i = closingBracket + 1
                        continue
                    }
                }
                append(cleanHtml[i])
                i++
            }

            // Close any remaining open tags
            tags.reversed().forEach { (tagName, start) ->
                getStyleForTag(tagName)?.let { addStyle(it, start, length) }
            }
        }
    }

    private fun getStyleForTag(tag: String): SpanStyle? = when (tag) {
        "b", "strong" -> SpanStyle(fontWeight = FontWeight.Bold)
        "i", "em" -> SpanStyle(fontStyle = FontStyle.Italic)
        "u" -> SpanStyle(textDecoration = TextDecoration.Underline)
        else -> null
    }

    fun fromAnnotatedString(annotatedString: AnnotatedString): String {
        val text = annotatedString.text
        if (text.isEmpty()) return ""

        val html = StringBuilder()
        val spans = annotatedString.spanStyles.sortedBy { it.start }

        // Define boundaries where styles change
        val boundaries =
            (spans.flatMap { listOf(it.start, it.end) } + 0 + text.length)
                .distinct()
                .filter { it in 0..text.length }
                .sorted()

        for (i in 0 until boundaries.size - 1) {
            val start = boundaries[i]
            val end = boundaries[i + 1]
            if (start >= end) continue

            val segment = text.substring(start, end)
            val activeSpans = spans.filter { it.start <= start && it.end >= end }

            var formattedSegment =
                segment
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\n", "<br/>")

            // Lenient style check to ensure we catch all bold/italic/underline variants
            val isBold =
                activeSpans.any {
                    it.item.fontWeight == FontWeight.Bold ||
                        (it.item.fontWeight?.weight ?: 0) >= 700
                }
            val isItalic = activeSpans.any { it.item.fontStyle == FontStyle.Italic }
            val isUnderline = activeSpans.any { it.item.textDecoration?.contains(TextDecoration.Underline) == true }

            if (isUnderline) formattedSegment = "<u>$formattedSegment</u>"
            if (isItalic) formattedSegment = "<i>$formattedSegment</i>"
            if (isBold) formattedSegment = "<b>$formattedSegment</b>"

            html.append(formattedSegment)
        }

        // Final cleanup to merge adjacent tags and handle empty segments
        return html
            .toString()
            .replace("</b><b>", "")
            .replace("</i><i>", "")
            .replace("</u><u>", "")
            .replace("</u></i></b><b><i><u>", "")
    }

    fun toggleStyle(value: TextFieldValue, style: SpanStyle): TextFieldValue {
        val selection = value.selection
        if (selection.collapsed) return value

        val annotatedString = value.annotatedString
        val start = selection.min
        val end = selection.max

        // Find if this style already exists in the selection
        val isPresent =
            annotatedString.spanStyles.any { span ->
                span.start <= start && span.end >= end && isSimilarStyle(span.item, style)
            }

        val newAnnotatedString =
            buildAnnotatedString {
                append(annotatedString.text)

                // Re-add existing styles, but split/remove the one we're toggling
                annotatedString.spanStyles.forEach { span ->
                    if (isSimilarStyle(span.item, style)) {
                        if (isPresent) {
                            // Removing: keep parts outside selection
                            if (span.start < start) {
                                addStyle(span.item, span.start, start)
                            }
                            if (span.end > end) {
                                addStyle(span.item, end, span.end)
                            }
                        } else {
                            // Adding: we'll merge it later, but keep existing for now
                            addStyle(span.item, span.start, span.end)
                        }
                    } else {
                        addStyle(span.item, span.start, span.end)
                    }
                }

                // If not present, add it
                if (!isPresent) {
                    addStyle(style, start, end)
                }
            }

        return value.copy(annotatedString = newAnnotatedString)
    }

    internal fun isSimilarStyle(s1: SpanStyle, s2: SpanStyle): Boolean {
        if (s1.fontWeight == FontWeight.Bold && s2.fontWeight == FontWeight.Bold) return true
        if (s1.fontStyle == FontStyle.Italic && s2.fontStyle == FontStyle.Italic) return true
        if (s1.textDecoration == TextDecoration.Underline && s2.textDecoration == TextDecoration.Underline) return true
        return false
    }
}
