package com.maubis.markdown

import com.maubis.markdown.segmenter.TextSegmentConfig
import com.maubis.markdown.spans.SpanConfig

object MarkdownConfig {
  val segmenterConfig by lazy { TextSegmentConfig(TextSegmentConfig.Builder()) }
  val spanConfig = SpanConfig()
}