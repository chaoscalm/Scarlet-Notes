package com.maubis.markdown.inline

import org.junit.Assert

abstract class InlineMarkdownTestSuite {
  internal fun assertSegmentsAreEqual(expectedInline: MarkdownInlineSegment, processedInline: MarkdownInlineSegment) {
    Assert.assertEquals("Expected equals", expectedInline.debug(), processedInline.debug())
  }
}
