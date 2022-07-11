package com.maubis.scarlet.base.common.utils

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.widget.ImageView
import kotlin.math.roundToInt

const val INACTIVE_ICON_ALPHA = 0.6f
const val ACTIVE_ICON_ALPHA = 0.9f

fun ImageView.setIconTint(color: Int) {
  imageTintList = ColorStateList.valueOf(color)
  alpha = ACTIVE_ICON_ALPHA
}

fun ImageView.resetIconTint() {
  imageTintList = null
  alpha = 1f
}

fun Drawable.tinted(color: Int, isInactive: Boolean): Drawable {
  val mutatedIcon = mutate()
  val alphaMultiplier = if (isInactive) INACTIVE_ICON_ALPHA else ACTIVE_ICON_ALPHA
  mutatedIcon.alpha = (255 * alphaMultiplier).roundToInt()
  mutatedIcon.setTint(color)
  return mutatedIcon
}