package com.maubis.scarlet.base.common.sheets

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface.BOLD
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
import com.maubis.scarlet.base.common.ui.ThemeColor

class LithoOptionsItem(
  val title: Int,
  val subtitle: Int,
  val content: String = "",
  val icon: Int,
  val isSelectable: Boolean = false,
  val selected: Boolean = false,
  val visible: Boolean = true,
  val listener: () -> Unit)

@LayoutSpec
object OptionItemLayoutSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop option: LithoOptionsItem): Component {
    val titleColor = appTheme.getColor(ThemeColor.SECONDARY_TEXT)
    val iconColor = appTheme.getColor(ThemeColor.ICON)
    val subtitleColor = appTheme.getColor(ThemeColor.HINT_TEXT)
    val selectedColor = appTheme.getColor(ThemeColor.ACCENT_TEXT)

    val subtitle = when (option.subtitle) {
      0 -> option.content
      else -> context.getString(option.subtitle)
    }

    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .paddingDip(YogaEdge.VERTICAL, 12f)
      .child(
        RoundIcon.create(context)
          .iconRes(option.icon)
          .bgColor(titleColor)
          .iconColor(iconColor)
          .iconSizeRes(R.dimen.toolbar_round_icon_size)
          .iconPaddingRes(R.dimen.toolbar_round_icon_padding)
          .bgAlpha(15)
          .flexShrink(0f)
          .marginDip(YogaEdge.END, 16f))
      .child(
        Column.create(context)
          .flexGrow(1f)
          .child(
            Text.create(context)
              .textRes(option.title)
              .textSizeRes(R.dimen.font_size_normal)
              .typeface(appTypeface.title())
              .textStyle(BOLD)
              .textColor(titleColor))
          .child(
            Text.create(context)
              .text(subtitle)
              .textSizeRes(R.dimen.font_size_small)
              .typeface(appTypeface.title())
              .textColor(subtitleColor)))

    if (option.isSelectable) {
      row.child(RoundIcon.create(context)
                  .iconRes(R.drawable.ic_selected)
                  .bgColor(if (option.selected) selectedColor else titleColor)
                  .bgAlpha(if (option.selected) 200 else 25)
                  .isInactive(!option.selected)
                  .iconColor(if (option.selected) Color.WHITE else iconColor)
                  .iconSizeRes(R.dimen.toolbar_round_small_icon_size)
                  .iconPaddingRes(R.dimen.toolbar_round_small_icon_padding)
                  .flexShrink(0f)
                  .marginDip(YogaEdge.START, 12f))
    }

    row.clickHandler(OptionItemLayout.onItemClick(context))
    return row.build()
  }

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(ClickEvent::class)
  fun onItemClick(context: ComponentContext, @Prop onClick: () -> Unit) {
    onClick()
  }
}

abstract class LithoOptionBottomSheet : LithoBottomSheet() {

  abstract fun title(): Int
  abstract fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem>

  override fun isAlwaysExpanded(): Boolean = true

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val column = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .child(getLithoBottomSheetTitle(componentContext).textRes(title()))
    getOptions(componentContext, dialog).forEach {
      if (it.visible) {
        column.child(OptionItemLayout.create(componentContext)
                       .option(it)
                       .onClick {
                         it.listener()
                       })
      }
    }
    return column.build()
  }
}
