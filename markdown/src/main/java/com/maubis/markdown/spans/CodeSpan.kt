package com.maubis.markdown.spans

import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import com.maubis.markdown.MarkdownConfig

class CodeSpan : MetricAffectingSpan(), ICustomSpan {
  override fun updateMeasureState(paint: TextPaint) {
    setTextColor(paint)
  }

  override fun updateDrawState(paint: TextPaint) {
    setTextColor(paint)
    paint.bgColor = MarkdownConfig.spanConfig.codeBackgroundColor
    paint.typeface = MarkdownConfig.spanConfig.codeTypeface
    paint.textSize = paint.textSize * 0.87f
  }

  private fun setTextColor(paint: TextPaint) {
    paint.color = paint.color
    paint.alpha = 225
  }
}
