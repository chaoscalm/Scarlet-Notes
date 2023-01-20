package com.maubis.markdown.inline

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maubis.markdown.inline.*
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class InlineRecursiveTextTests : InlineMarkdownTestSuite() {
  @Test
  fun testSimple2Levels() {
    val text = "**~hello~**"
    val processed = InlineMarkdownParser(text).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.BOLD),
        listOf(DelimitedInlineSegment(InvalidInline(InlineSegmentType.STRIKE),
            listOf(NormalInlineSegment("hello"))))), processed)
  }


  @Test
  fun testDeeperLevelComplexLevels() {
    val text = "**t1~t2~**"
    val processed = InlineMarkdownParser(text).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.BOLD),
        listOf(NormalInlineSegment("t1"),
            DelimitedInlineSegment(InvalidInline(InlineSegmentType.STRIKE),listOf(NormalInlineSegment("t2"))))), processed)
  }

  @Test
  fun testCodeMultileveLevels() {
    val text = "`t1 **t2** *u*`"
    val processed = InlineMarkdownParser(text).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.CODE),listOf(NormalInlineSegment("t1 **t2** *u*"))), processed)
  }

  @Test
  fun testMismatchingLevelsKind1() {
    val text1 = "**t~t**"
    val processed1 = InlineMarkdownParser(text1).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.BOLD),listOf(NormalInlineSegment("t~t"))), processed1)

    val text2 = "`t~t`"
    val processed2 = InlineMarkdownParser(text2).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.CODE),listOf(NormalInlineSegment("t~t"))), processed2)


    val text3 = "~t*t~"
    val processed3 = InlineMarkdownParser(text3).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.STRIKE),listOf(NormalInlineSegment("t*t"))), processed3)
  }

  @Test
  fun testMismatchingLevelsKind2() {
    val text2 = "~t~t~"
    val processed2 = InlineMarkdownParser(text2).parseText()
    assert(DelimitedInlineSegment(InvalidInline(InlineSegmentType.INVALID),listOf(DelimitedInlineSegment(InvalidInline(InlineSegmentType.STRIKE),listOf(NormalInlineSegment("t"))), NormalInlineSegment("t~"))), processed2)
  }
}
