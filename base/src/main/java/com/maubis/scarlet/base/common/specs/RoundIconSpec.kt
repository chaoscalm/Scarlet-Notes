package com.maubis.scarlet.base.common.specs

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.facebook.litho.ClickEvent
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.Image
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.common.ui.LithoCircleDrawable
import kotlin.math.roundToInt

@LayoutSpec
object RoundIconSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop(resType = ResType.DRAWABLE) icon: Drawable,
    @Prop(resType = ResType.COLOR) iconColor: Int,
    @Prop(resType = ResType.COLOR) bgColor: Int,
    @Prop(resType = ResType.DIMEN_SIZE) iconSize: Int,
    @Prop(resType = ResType.DIMEN_SIZE, optional = true) iconPadding: Int?,
    @Prop(resType = ResType.DIMEN_OFFSET, optional = true) iconMarginVertical: Int?,
    @Prop(resType = ResType.DIMEN_OFFSET, optional = true) iconMarginHorizontal: Int?,
    @Prop(optional = true) isInactive: Boolean?,
    @Prop(optional = true) bgAlpha: Int?,
    @Prop(optional = true) onClick: (() -> Unit)?,
    @Prop(optional = true) showBorder: Boolean?): Component {
    val alphaMultiplier = if (isInactive == true) 0.6f else 0.9f
    icon.alpha = (255 * alphaMultiplier).roundToInt()
    val image = Image.create(context)
      .heightPx(iconSize)
      .widthPx(iconSize)
      .paddingPx(YogaEdge.ALL, iconPadding ?: 0)
      .marginPx(YogaEdge.VERTICAL, iconMarginVertical ?: 0)
      .marginPx(YogaEdge.HORIZONTAL, iconMarginHorizontal ?: 0)
      .drawable(icon.color(iconColor))
      .background(
        LithoCircleDrawable(
          bgColor, bgAlpha ?: Color.alpha(bgColor), showBorder
          ?: false))
    if (onClick != null) {
      image.clickHandler(RoundIcon.onClickEvent(context))
    }
    return image.build()
  }

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(ClickEvent::class)
  fun onClickEvent(context: ComponentContext, @Prop(optional = true) onClick: (() -> Unit)?) {
    onClick?.invoke()
  }
}
