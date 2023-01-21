package com.maubis.markdown.inline

import org.junit.Test

class InlineHtmlTextTests : InlineMarkdownTestSuite() {
  @Test
  fun testEmptyText() {
    val text = "Hi<b>Hello<i>World</i></b>"
    val processed = InlineMarkdownParser(text).parseText()
    assertSegmentsAreEqual(DelimitedInlineSegment(InvalidInline(InlineSegmentType.INVALID), listOf(
        NormalInlineSegment("Hi"),
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.BOLD), listOf(NormalInlineSegment("Hello"),
            DelimitedInlineSegment(InvalidInline(InlineSegmentType.ITALICS), listOf(NormalInlineSegment("World"))))))), processed)
  }

  @Test
  fun testMultilineText() {
    val text = "<u>Hello</u><code>World</code><em>Italics</em><strong>Strong</strong>"
    val processed = InlineMarkdownParser(text).parseText()
    assertSegmentsAreEqual(DelimitedInlineSegment(InvalidInline(InlineSegmentType.INVALID), listOf(
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.UNDERLINE), listOf(NormalInlineSegment("Hello"))),
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.CODE), listOf(NormalInlineSegment("World"))),
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.ITALICS), listOf(NormalInlineSegment("Italics"))),
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.BOLD), listOf(NormalInlineSegment("Strong"))))), processed)
  }

  @Test
  fun testBlankText() {
    val text = "  "
    val processed = InlineMarkdownParser(text).parseText()
    assertSegmentsAreEqual(NormalInlineSegment(text), processed)
  }
}
