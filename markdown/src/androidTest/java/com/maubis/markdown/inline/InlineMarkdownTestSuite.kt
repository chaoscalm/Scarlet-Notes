package com.maubis.markdown.inline

import android.util.Log
import org.junit.Assert

abstract class InlineMarkdownTestSuite {
  internal fun assert(expectedInline: MarkdownInlineSegment, processedInline: MarkdownInlineSegment) {
    Log.d("Asset:expected.", expectedInline.debug())
    Log.d("Asset:processed", processedInline.debug())

    Assert.assertEquals("Expected equals", expectedInline.debug(), processedInline.debug())
  }
}
