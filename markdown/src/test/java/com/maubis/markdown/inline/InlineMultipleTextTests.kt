package com.maubis.markdown.inline

import org.junit.Test

class InlineMultipleTextTests : InlineMarkdownTestSuite() {
  @Test
  fun testSimpleText() {
    val text = "t1 **t2** *t3* _t4_ ~t5~ `t6`"
    val processed = InlineMarkdownParser(text).parseText()
    assertSegmentsAreEqual(DelimitedInlineSegment(PlainInline(InlineSegmentType.INVALID), listOf(
        NormalInlineSegment("t1 "),
        DelimitedInlineSegment(PlainInline(InlineSegmentType.BOLD), listOf(NormalInlineSegment("t2"))),
        NormalInlineSegment(" "),
        DelimitedInlineSegment(PlainInline(InlineSegmentType.ITALICS), listOf(NormalInlineSegment("t3"))),
        NormalInlineSegment(" "),
        DelimitedInlineSegment(PlainInline(InlineSegmentType.UNDERLINE), listOf(NormalInlineSegment("t4"))),
        NormalInlineSegment(" "),
        DelimitedInlineSegment(PlainInline(InlineSegmentType.STRIKE), listOf(NormalInlineSegment("t5"))),
        NormalInlineSegment(" "),
        DelimitedInlineSegment(PlainInline(InlineSegmentType.CODE), listOf(NormalInlineSegment("t6"))))), processed)
  }


  @Test
  fun testStickingTogether() {
    val text = "t1**t2***t3*_t4_~t5~`t6`"
    val processed = InlineMarkdownParser(text).parseText()
    assertSegmentsAreEqual(DelimitedInlineSegment(PlainInline(InlineSegmentType.INVALID), listOf(
        NormalInlineSegment("t1"),
        DelimitedInlineSegment(PlainInline(InlineSegmentType.BOLD), listOf(NormalInlineSegment("t2"))),
        DelimitedInlineSegment(PlainInline(InlineSegmentType.ITALICS), listOf(NormalInlineSegment("t3"))),
        DelimitedInlineSegment(PlainInline(InlineSegmentType.UNDERLINE), listOf(NormalInlineSegment("t4"))),
        DelimitedInlineSegment(PlainInline(InlineSegmentType.STRIKE), listOf(NormalInlineSegment("t5"))),
        DelimitedInlineSegment(PlainInline(InlineSegmentType.CODE), listOf(NormalInlineSegment("t6"))))), processed)
  }

  @Test
  fun testEscapedText() {
    val textA = "aaa\\_bb_c"
    val processedA = InlineMarkdownParser(textA).parseText()
    assertSegmentsAreEqual(DelimitedInlineSegment(PlainInline(InlineSegmentType.INVALID), listOf(
        NormalInlineSegment("aaa"),
        DelimitedInlineSegment(PlainInline(InlineSegmentType.IGNORE_CHAR), listOf(NormalInlineSegment("_"))),
        NormalInlineSegment("bb_c"))), processedA)
  }
}
