package com.maubis.markdown.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.TextPaint
import android.text.style.LeadingMarginSpan
import android.text.style.MetricAffectingSpan
import com.maubis.markdown.MarkdownConfig

class SeparatorSegmentSpan : MetricAffectingSpan(), LeadingMarginSpan, ICustomSpan {
  private val rect = Rect()
  private val paint = Paint()

  override fun updateMeasureState(paint: TextPaint) {
    setTextColor(paint)
  }

  override fun updateDrawState(paint: TextPaint) {
    setTextColor(paint)
  }

  private fun setTextColor(paint: TextPaint) {
    paint.color = MarkdownConfig.spanConfig.codeTextColor
    paint.alpha = 100
  }

  override fun getLeadingMargin(first: Boolean): Int {
    return MarkdownConfig.spanConfig.codeBlockLeadingMargin
  }

  override fun drawLeadingMargin(
      canvas: Canvas,
      p: Paint,
      x: Int,
      dir: Int,
      top: Int,
      baseline: Int,
      bottom: Int,
      text: CharSequence,
      start: Int,
      end: Int,
      first: Boolean,
      layout: Layout) {
    paint.style = Paint.Style.FILL
    paint.color = MarkdownConfig.spanConfig.separatorColor

    val leftPosition = when {
      dir > 0 -> x
      else -> x - canvas.width
    }
    val rightPosition = when {
      dir > 0 -> canvas.width
      else -> x
    }

    val middle = (top + bottom) / 2
    val width = MarkdownConfig.spanConfig.separatorWidth / 2

    rect.set(leftPosition, middle - width, rightPosition, middle + width)
    canvas.drawRect(rect, paint)
  }
}
