package com.maubis.scarlet.base.common.specs

import android.graphics.Color
import android.os.Handler
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Card
import com.facebook.litho.widget.SolidColor
import com.facebook.litho.widget.TouchableFeedback
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.common.ui.ThemeColor

object EmptySpec {
  fun create(context: ComponentContext): SolidColor.Builder {
    return SolidColor.create(context)
      .color(Color.TRANSPARENT)
  }
}

fun separatorSpec(context: ComponentContext): Component.Builder<*> {
  return SolidColor.create(context)
    .alignSelf(YogaAlign.CENTER)
    .colorRes(com.github.bijoysingh.uibasics.R.color.material_grey_200)
    .heightDip(1f)
    .widthDip(164f)
    .marginDip(YogaEdge.HORIZONTAL, 32f)
    .marginDip(YogaEdge.TOP, 16f)
    .marginDip(YogaEdge.BOTTOM, 16f)
}

fun rippleWrapper(context: ComponentContext, component: Component.Builder<*>): TouchableFeedback.Builder {
  val rippleColor = if (appTheme.isNightTheme()) 0x40ffffff else 0x40000000
  return TouchableFeedback.create(context)
    .color(Color.TRANSPARENT)
    .highlightColor(rippleColor)
    .content(component)
}

fun delayForRippleEffect(context: ComponentContext, function: () -> Unit) {
  Handler(context.mainLooper).postDelayed(function, 90)
}

data class ToolbarColorConfig(
  var toolbarBackgroundColor: Int = appTheme.getColor(ThemeColor.TOOLBAR_BACKGROUND),
  var toolbarIconColor: Int = appTheme.getColor(ThemeColor.ICON))

fun bottomBarRoundIcon(context: ComponentContext, colorConfig: ToolbarColorConfig): RoundIcon.Builder {
  return RoundIcon.create(context)
    .bgColor(colorConfig.toolbarIconColor)
    .iconColor(colorConfig.toolbarIconColor)
    .iconSizeRes(R.dimen.toolbar_round_icon_size)
    .iconPaddingRes(R.dimen.toolbar_round_icon_padding)
    .iconMarginVerticalRes(R.dimen.toolbar_round_icon_margin_vertical)
    .iconMarginHorizontalRes(R.dimen.toolbar_round_icon_margin_horizontal)
    .bgAlpha(15)
}

fun bottomBar(context: ComponentContext, child: Component, colorConfig: ToolbarColorConfig): Column.Builder {
  return Column.create(context)
    .widthPercent(100f)
    .paddingDip(YogaEdge.ALL, 0f)
    .backgroundColor(Color.TRANSPARENT)
    .child(
      Card.create(context)
        .widthPercent(100f)
        .backgroundColor(Color.TRANSPARENT)
        .clippingColor(Color.TRANSPARENT)
        .cardBackgroundColor(colorConfig.toolbarBackgroundColor)
        .cornerRadiusDip(0f)
        .elevationDip(0f)
        .content(child))
}