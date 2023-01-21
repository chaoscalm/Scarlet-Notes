package com.maubis.markdown.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.style.LeadingMarginSpan
import com.maubis.markdown.MarkdownConfig
import kotlin.math.max
import kotlin.math.min

class QuoteBlockSpan : LeadingMarginSpan, CustomMarkdownSpan {

  private val rect = Rect()
  private val paint = Paint()

  override fun getLeadingMargin(first: Boolean): Int {
    return MarkdownConfig.spanConfig.quoteBlockLeadingMargin
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
    paint.set(p)
    paint.style = Paint.Style.FILL
    paint.color = MarkdownConfig.spanConfig.quoteColor
    paint.typeface = MarkdownConfig.spanConfig.textTypeface

    val startPosition = x + dir * MarkdownConfig.spanConfig.quoteWidth
    val endPosition = startPosition + dir * MarkdownConfig.spanConfig.quoteWidth

    rect.set(min(startPosition, endPosition), top, max(startPosition, endPosition), bottom)
    canvas.drawRect(rect, paint)
  }
}
