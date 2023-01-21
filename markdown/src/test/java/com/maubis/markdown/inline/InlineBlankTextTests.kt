package com.maubis.markdown.inline

import org.junit.Test

class InlineBlankTextTests : InlineMarkdownTestSuite() {
  @Test
  fun testEmptyText() {
    val text = ""
    val processed = InlineMarkdownParser(text).parseText()
    assertSegmentsAreEqual(DelimitedInlineSegment(InvalidInline(InlineSegmentType.INVALID), listOf()), processed)
  }

  @Test
  fun testMultilineText() {
    val text = "\n\n"
    val processed = InlineMarkdownParser(text).parseText()
    assertSegmentsAreEqual(NormalInlineSegment(text), processed)
  }

  @Test
  fun testBlankText() {
    val text = "  "
    val processed = InlineMarkdownParser(text).parseText()
    assertSegmentsAreEqual(NormalInlineSegment(text), processed)
  }
}
