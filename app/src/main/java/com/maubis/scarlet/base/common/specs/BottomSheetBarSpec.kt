package com.maubis.scarlet.base.common.specs

import com.facebook.litho.ClickEvent
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.sheets.getLithoBottomSheetButton
import com.maubis.scarlet.base.common.ui.ThemeColor

@LayoutSpec
object BottomSheetBarSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop(resType = ResType.STRING) primaryAction: String,
    @Prop(optional = true) isActionNegative: Boolean?,
    @Prop(resType = ResType.STRING, optional = true) secondaryAction: String?): Component {
    val actionNegative = isActionNegative ?: false

    val row = Row.create(context)
      .alignItems(YogaAlign.CENTER)

    if (secondaryAction != null) {
      row.child(
        rippleWrapper(
          context,
          Text.create(context)
            .text(secondaryAction)
            .typeface(appTypeface.title())
            .textSizeRes(R.dimen.font_size_large)
            .paddingDip(YogaEdge.VERTICAL, 12f)
            .paddingDip(YogaEdge.HORIZONTAL, 16f)
            .textColor(appTheme.getColor(ThemeColor.TERTIARY_TEXT))
            .clickHandler(BottomSheetBar.onSecondaryClickEvent(context))
        )
      )
    }
    row.child(EmptySpec.create(context).flexGrow(1f))

    row.child(
      getLithoBottomSheetButton(context)
        .text(primaryAction)
        .backgroundRes(if (actionNegative) R.drawable.disabled_rounded_bg else R.drawable.accent_rounded_bg)
        .clickHandler(BottomSheetBar.onPrimaryClickEvent(context)))
    return row.build()
  }

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(ClickEvent::class)
  fun onPrimaryClickEvent(context: ComponentContext, @Prop onPrimaryClick: () -> Unit) {
    onPrimaryClick()
  }

  @OnEvent(ClickEvent::class)
  fun onSecondaryClickEvent(context: ComponentContext, @Prop(optional = true) onSecondaryClick: () -> Unit) {
    delayForRippleEffect(context) { onSecondaryClick() }
  }
}

