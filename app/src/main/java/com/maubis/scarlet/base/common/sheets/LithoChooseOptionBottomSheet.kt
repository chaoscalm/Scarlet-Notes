package com.maubis.scarlet.base.common.sheets

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import com.facebook.litho.*
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.specs.RoundIcon
import com.maubis.scarlet.base.common.specs.delayForRippleEffect
import com.maubis.scarlet.base.common.specs.rippleWrapper
import com.maubis.scarlet.base.common.ui.ThemeColor

class LithoChooseOptionsItem(
  val title: Int,
  val selected: Boolean = false,
  val listener: () -> Unit)

@LayoutSpec
object ChooseOptionItemLayoutSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop option: LithoChooseOptionsItem): Component {
    val titleColor = appTheme.getColor(ThemeColor.SECONDARY_TEXT)
    val iconColor = appTheme.getColor(ThemeColor.ICON)
    val selectedColor = context.getColor(R.color.colorAccent)

    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .paddingDip(YogaEdge.VERTICAL, 12f)
      .child(
        Text.create(context)
          .textRes(option.title)
          .textSizeRes(R.dimen.font_size_normal)
          .typeface(appTypeface.title())
          .textStyle(Typeface.BOLD)
          .textColor(titleColor)
          .flexGrow(1f))
      .child(RoundIcon.create(context)
               .iconRes(R.drawable.ic_selected)
               .bgColor(if (option.selected) selectedColor else titleColor)
               .bgAlpha(if (option.selected) 200 else 25)
               .isInactive(!option.selected)
               .iconColor(if (option.selected) Color.WHITE else iconColor)
               .iconSizeRes(R.dimen.toolbar_round_small_icon_size)
               .iconPaddingRes(R.dimen.toolbar_round_small_icon_padding)
               .marginDip(YogaEdge.START, 12f))
    row.clickHandler(ChooseOptionItemLayout.onItemClick(context))
    return rippleWrapper(context, row).build()
  }

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(ClickEvent::class)
  fun onItemClick(context: ComponentContext, @Prop onClick: () -> Unit) {
    delayForRippleEffect(context) { onClick() }
  }
}

abstract class LithoChooseOptionBottomSheet : LithoBottomSheet() {

  abstract fun title(): Int
  abstract fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoChooseOptionsItem>

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val column = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .child(getLithoBottomSheetTitle(componentContext).textRes(title()))
    getOptions(componentContext, dialog).forEach {
      column.child(ChooseOptionItemLayout.create(componentContext)
                     .option(it)
                     .onClick {
                       it.listener()
                       refresh(componentContext.androidContext, dialog)
                     })
    }
    return column.build()
  }
}
