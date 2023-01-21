package com.maubis.markdown.inline

import org.junit.Test

class InlineHtmlTextTests : InlineMarkdownTestSuite() {
  @Test
  fun testEmptyText() {
    val text = "Hi<b>Hello<i>World</i></b>"
    val processed = InlineMarkdownParser(text).parseText()
    assertSegmentsAreEqual(DelimitedInlineSegment(PlainInline(InlineSegmentType.INVALID), listOf(
        NormalInlineSegment("Hi"),
        DelimitedInlineSegment(PlainInline(InlineSegmentType.BOLD), listOf(NormalInlineSegment("Hello"),
            DelimitedInlineSegment(PlainInline(InlineSegmentType.ITALICS), listOf(NormalInlineSegment("World"))))))), processed)
  }

  @Test
  fun testMultilineText() {
    val text = "<u>Hello</u><code>World</code><em>Italics</em><strong>Strong</strong>"
    val processed = InlineMarkdownParser(text).parseText()
    assertSegmentsAreEqual(DelimitedInlineSegment(PlainInline(InlineSegmentType.INVALID), listOf(
        DelimitedInlineSegment(PlainInline(InlineSegmentType.UNDERLINE), listOf(NormalInlineSegment("Hello"))),
        DelimitedInlineSegment(PlainInline(InlineSegmentType.CODE), listOf(NormalInlineSegment("World"))),
        DelimitedInlineSegment(PlainInline(InlineSegmentType.ITALICS), listOf(NormalInlineSegment("Italics"))),
        DelimitedInlineSegment(PlainInline(InlineSegmentType.BOLD), listOf(NormalInlineSegment("Strong"))))), processed)
  }

  @Test
  fun testBlankText() {
    val text = "  "
    val processed = InlineMarkdownParser(text).parseText()
    assertSegmentsAreEqual(NormalInlineSegment(text), processed)
  }
}
