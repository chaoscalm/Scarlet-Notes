package com.maubis.markdown.inline

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maubis.markdown.inline.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InlineMultipleTextTests : InlineMarkdownTestSuite() {
  @Test
  fun testSimpleText() {
    val text = "t1 **t2** *t3* _t4_ ~t5~ `t6`"
    val processed = InlineMarkdownParser(text).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.INVALID), listOf(
        NormalInlineSegment("t1 "),
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.BOLD), listOf(NormalInlineSegment("t2"))),
        NormalInlineSegment(" "),
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.ITALICS), listOf(NormalInlineSegment("t3"))),
        NormalInlineSegment(" "),
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.UNDERLINE), listOf(NormalInlineSegment("t4"))),
        NormalInlineSegment(" "),
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.STRIKE), listOf(NormalInlineSegment("t5"))),
        NormalInlineSegment(" "),
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.CODE), listOf(NormalInlineSegment("t6"))))), processed)
  }


  @Test
  fun testStickingTogether() {
    val text = "t1**t2***t3*_t4_~t5~`t6`"
    val processed = InlineMarkdownParser(text).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.INVALID), listOf(
        NormalInlineSegment("t1"),
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.BOLD), listOf(NormalInlineSegment("t2"))),
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.ITALICS), listOf(NormalInlineSegment("t3"))),
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.UNDERLINE), listOf(NormalInlineSegment("t4"))),
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.STRIKE), listOf(NormalInlineSegment("t5"))),
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.CODE), listOf(NormalInlineSegment("t6"))))), processed)
  }

  @Test
  fun testEscapedText() {
    val textA = "aaa\\_bb_c"
    val processedA = InlineMarkdownParser(textA).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.INVALID), listOf(
        NormalInlineSegment("aaa"),
        DelimitedInlineSegment(InvalidInline(InlineSegmentType.IGNORE_CHAR), listOf(NormalInlineSegment("_"))),
        NormalInlineSegment("bb_c"))), processedA)
  }
}
