package com.maubis.markdown.inline

import org.junit.Test

class InlineSimpleTextTests : InlineMarkdownTestSuite() {
  @Test
  fun testSimpleText() {
    val text = "Hello World"
    val processed = InlineMarkdownParser(text).parseText()
    assertSegmentsAreEqual(NormalInlineSegment(text), processed)
  }

  @Test
  fun testBoldText() {
    val text = "Hello World"
    val textToTest = "**$text**"
    val processed = InlineMarkdownParser(textToTest).parseText()
    assertSegmentsAreEqual(DelimitedInlineSegment(PlainInline(InlineSegmentType.BOLD),listOf(NormalInlineSegment(text))), processed)
  }

  @Test
  fun testItalicsText() {
    val text = "Hello World"
    val textToTest = "_${text}_"
    val processed = InlineMarkdownParser(textToTest).parseText()
    assertSegmentsAreEqual(DelimitedInlineSegment(PlainInline(InlineSegmentType.UNDERLINE),listOf(NormalInlineSegment(text))), processed)
  }
  @Test
  fun testStrikeThroughText() {
    val text = "Hello World"
    val textToTest = "~$text~"
    val processed = InlineMarkdownParser(textToTest).parseText()
    assertSegmentsAreEqual(DelimitedInlineSegment(PlainInline(InlineSegmentType.STRIKE),listOf(NormalInlineSegment(text))), processed)
  }

  @Test
  fun testUnderlineText() {
    val text = "Hello World"
    val textToTest = "*$text*"
    val processed = InlineMarkdownParser(textToTest).parseText()
    assertSegmentsAreEqual(DelimitedInlineSegment(PlainInline(InlineSegmentType.ITALICS),listOf(NormalInlineSegment(text))), processed)
  }

  @Test
  fun testCodeText() {
    val text = "Hello World"
    val textToTest = "`$text`"
    val processed = InlineMarkdownParser(textToTest).parseText()
    assertSegmentsAreEqual(DelimitedInlineSegment(PlainInline(InlineSegmentType.CODE),listOf(NormalInlineSegment(text))), processed)
  }

  @Test
  fun testIncompleteText() {
    val textA = "aaa_bb*c"
    val processedA = InlineMarkdownParser(textA).parseText()
    assertSegmentsAreEqual(DelimitedInlineSegment(PlainInline(InlineSegmentType.INVALID), listOf(NormalInlineSegment(textA))), processedA)

    val textB = "aaa_"
    val processedB = InlineMarkdownParser(textB).parseText()
    assertSegmentsAreEqual(DelimitedInlineSegment(PlainInline(InlineSegmentType.INVALID), listOf(NormalInlineSegment(textB))), processedB)
  }
}
