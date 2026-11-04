package com.tripwise.core.ui.component

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Renders the small Markdown subset the assistant uses: **bold**, *italic*, "# " headings,
 * and "- " / "* " / "1. " lists. Anything else is shown as plain text (never as HTML).
 */
@Composable
fun MarkdownText(markdown: String, style: TextStyle, modifier: Modifier = Modifier) {
    val color = LocalContentColor.current
    val text = remember(markdown) { parseMarkdown(markdown) }
    Text(text, modifier, style = style, color = color)
}

internal fun parseMarkdown(markdown: String): AnnotatedString = buildAnnotatedString {
    markdown.lines().forEachIndexed { i, raw ->
        if (i > 0) append('\n')
        var line = raw
        val heading = Regex("^#{1,6}\\s+").find(line)
        when {
            heading != null -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { appendInline(line.substring(heading.range.last + 1)) }
                return@forEachIndexed
            }
            Regex("^\\s*[-*]\\s+").containsMatchIn(line) -> {
                val indent = line.takeWhile { it == ' ' }.length / 2
                append("  ".repeat(indent) + "•  ")
                line = line.replaceFirst(Regex("^\\s*[-*]\\s+"), "")
            }
        }
        appendInline(line)
    }
}

private fun AnnotatedString.Builder.appendInline(text: String) {
    val pattern = Regex("\\*\\*(.+?)\\*\\*|(?<![*\\w])\\*(?!\\s)(.+?)(?<!\\s)\\*(?![*\\w])")
    var last = 0
    for (m in pattern.findAll(text)) {
        append(text.substring(last, m.range.first))
        val bold = m.groups[1]?.value
        if (bold != null) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(bold) }
        } else {
            withStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) { append(m.groups[2]?.value.orEmpty()) }
        }
        last = m.range.last + 1
    }
    append(text.substring(last))
}
