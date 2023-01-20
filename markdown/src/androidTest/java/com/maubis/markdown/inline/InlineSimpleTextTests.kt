package com.maubis.markdown.inline

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maubis.markdown.inline.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InlineSimpleTextTests : InlineMarkdownTestSuite() {
  @Test
  fun testSimpleText() {
    val text = "Hello World"
    val processed = InlineMarkdownParser(text).parseText()
    assert(NormalInlineSegment(text), processed)
  }

  @Test
  fun testBoldText() {
    val text = "Hello World"
    val textToTest = "**$text**"
    val processed = InlineMarkdownParser(textToTest).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.BOLD),listOf(NormalInlineSegment(text))), processed)
  }

  @Test
  fun testItalicsText() {
    val text = "Hello World"
    val textToTest = "_${text}_"
    val processed = InlineMarkdownParser(textToTest).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.UNDERLINE),listOf(NormalInlineSegment(text))), processed)
  }
  @Test
  fun testStrikeThroughText() {
    val text = "Hello World"
    val textToTest = "~$text~"
    val processed = InlineMarkdownParser(textToTest).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.STRIKE),listOf(NormalInlineSegment(text))), processed)
  }

  @Test
  fun testUnderlineText() {
    val text = "Hello World"
    val textToTest = "*$text*"
    val processed = InlineMarkdownParser(textToTest).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.ITALICS),listOf(NormalInlineSegment(text))), processed)
  }

  @Test
  fun testCodeText() {
    val text = "Hello World"
    val textToTest = "`$text`"
    val processed = InlineMarkdownParser(textToTest).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.CODE),listOf(NormalInlineSegment(text))), processed)
  }

  @Test
  fun testIncompleteText() {
    val textA = "aaa_bb*c"
    val processedA = InlineMarkdownParser(textA).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.INVALID), listOf(NormalInlineSegment(textA))), processedA)

    val textB = "aaa_"
    val processedB = InlineMarkdownParser(textB).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.INVALID), listOf(NormalInlineSegment(textB))), processedB)
  }
}
