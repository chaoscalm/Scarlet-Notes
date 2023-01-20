package com.maubis.markdown.inline

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maubis.markdown.inline.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InlineBlankTextTests : InlineMarkdownTestSuite() {
  @Test
  fun testEmptyText() {
    val text = ""
    val processed = InlineMarkdownParser(text).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.INVALID), listOf()), processed)
  }

  @Test
  fun testMultilineText() {
    val text = "\n\n"
    val processed = InlineMarkdownParser(text).parseText()
    assert(NormalInlineSegment(text), processed)
  }

  @Test
  fun testBlankText() {
    val text = "  "
    val processed = InlineMarkdownParser(text).parseText()
    assert(NormalInlineSegment(text), processed)
  }
}
