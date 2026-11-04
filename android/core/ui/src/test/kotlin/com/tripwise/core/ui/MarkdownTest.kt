package com.tripwise.core.ui

import com.tripwise.core.ui.component.parseMarkdown
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownTest {
    @Test fun `bold, italic, bullets and headings render as text`() {
        val out = parseMarkdown("## Day 1\n* **Fushimi Inari** early\n- *Nishiki* lunch\n2 * 3 = 6")
        assertEquals("Day 1\n•  Fushimi Inari early\n•  Nishiki lunch\n2 * 3 = 6", out.text)
        assertEquals(2, out.spanStyles.count { it.item.fontWeight != null }) // heading + bold
    }
}
