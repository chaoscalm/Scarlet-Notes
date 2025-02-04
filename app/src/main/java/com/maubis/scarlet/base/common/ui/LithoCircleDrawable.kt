package com.maubis.scarlet.base.common.ui

import android.graphics.*
import android.graphics.drawable.Drawable
import com.facebook.litho.drawable.ComparableDrawable
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import kotlin.math.min

class LithoCircleDrawable(color: Int, alpha: Int = 255, val showBorder: Boolean = false) : Drawable(), ComparableDrawable {
  private val mPaint: Paint
  private val mBorderPaint: Paint
  private var mRadius = 0

  init {
    this.mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    this.mPaint.color = color
    this.mPaint.alpha = alpha

    val isNightTheme = appTheme.isNightTheme()
    this.mBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    this.mBorderPaint.color = when {
      !showBorder -> Color.TRANSPARENT
      isNightTheme -> Color.LTGRAY
      else -> Color.GRAY
    }
  }

  override fun draw(canvas: Canvas) {
    val bounds = bounds
    canvas.drawCircle(bounds.centerX().toFloat(), bounds.centerY().toFloat(), mRadius.toFloat(), mBorderPaint)
    canvas.drawCircle(
      bounds.centerX().toFloat(),
      bounds.centerY().toFloat(),
      mRadius.toFloat() - (if (showBorder) 2 else 0),
      mPaint)
  }

  override fun setAlpha(alpha: Int) {
    mPaint.alpha = alpha
  }

  override fun setColorFilter(cf: ColorFilter?) {
    mPaint.colorFilter = cf
  }

  override fun getOpacity(): Int {
    return PixelFormat.TRANSLUCENT
  }

  override fun onBoundsChange(bounds: Rect) {
    super.onBoundsChange(bounds)
    mRadius = min(bounds.width(), bounds.height()) / 2
  }

  override fun isEquivalentTo(other: ComparableDrawable?): Boolean {
    return other is LithoCircleDrawable
      && other.mRadius == mRadius
      && other.mPaint.color == mPaint.color
      && other.mPaint.alpha == mPaint.alpha
  }
}