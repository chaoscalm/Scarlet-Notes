package com.maubis.markdown.spans

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan

class TypefaceSpanCompat(private val tface: Typeface) : TypefaceSpan(null), CustomMarkdownSpan {

  override fun updateDrawState(paint: TextPaint) {
    applyTypeface(paint)
  }

  override fun updateMeasureState(paint: TextPaint) {
    applyTypeface(paint)
  }

  private fun applyTypeface(paint: Paint) {
    val oldStyle = paint.typeface?.style ?: 0
    val isFake = oldStyle and tface.style.inv()
    if (isFake and Typeface.BOLD != 0) {
      paint.isFakeBoldText = true
    }

    if (isFake and Typeface.ITALIC != 0) {
      paint.textSkewX = -0.25f
    }

    paint.typeface = tface
  }
}