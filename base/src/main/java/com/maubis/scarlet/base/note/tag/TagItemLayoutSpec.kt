package com.maubis.scarlet.base.note.tag

import android.graphics.Typeface
import com.facebook.litho.ClickEvent
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.common.sheets.OptionItemLayout
import com.maubis.scarlet.base.common.specs.RoundIcon
import com.maubis.scarlet.base.common.ui.ThemeColor
import com.maubis.scarlet.base.database.entities.Tag

class TagItem(
    val tag: Tag,
    val isSelected: Boolean = false,
    val isEditable: Boolean = false,
    val editListener: () -> Unit = {},
    val listener: () -> Unit = {})

@LayoutSpec
object TagItemLayoutSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext, @Prop tagItem: TagItem): Component {
    val titleColor = ScarletApp.appTheme.getColor(ThemeColor.SECONDARY_TEXT)
    val selectedColor = when (ScarletApp.appTheme.isNightTheme()) {
      true -> context.getColor(com.github.bijoysingh.uibasics.R.color.material_blue_400)
      false -> context.getColor(com.github.bijoysingh.uibasics.R.color.material_blue_700)
    }

    val icon: Int
    val bgColor: Int
    val bgAlpha: Int
    val textColor: Int
    val typeface: Typeface
    when (tagItem.isSelected) {
      true -> {
        icon = R.drawable.ic_tag
        bgColor = selectedColor
        bgAlpha = 200
        textColor = selectedColor
        typeface = ScarletApp.appTypeface.subHeading()
      }
      false -> {
        icon = R.drawable.ic_tag_outline
        bgColor = titleColor
        bgAlpha = 15
        textColor = titleColor
        typeface = ScarletApp.appTypeface.title()
      }
    }

    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .paddingDip(YogaEdge.VERTICAL, 12f)
      .child(
        RoundIcon.create(context)
          .iconRes(icon)
          .bgColor(bgColor)
          .iconColor(titleColor)
          .iconSizeRes(R.dimen.toolbar_round_icon_size)
          .iconPaddingRes(R.dimen.toolbar_round_icon_padding)
          .bgAlpha(bgAlpha)
          .onClick { }
          .isClickDisabled(true)
          .marginDip(YogaEdge.END, 16f))
      .child(
        Text.create(context)
          .flexGrow(1f)
          .text(tagItem.tag.title)
          .textSizeRes(R.dimen.font_size_normal)
          .typeface(typeface)
          .textStyle(Typeface.BOLD)
          .textColor(textColor))

    if (tagItem.isEditable) {
      row.child(RoundIcon.create(context)
                  .iconRes(R.drawable.ic_edit)
                  .bgColor(titleColor)
                  .bgAlpha(15)
                  .iconAlpha(0.9f)
                  .iconColor(titleColor)
                  .iconSizeRes(R.dimen.toolbar_round_icon_size)
                  .iconPaddingRes(R.dimen.toolbar_round_icon_padding)
                  .onClick { tagItem.editListener() }
                  .isClickDisabled(false)
                  .marginDip(YogaEdge.START, 12f))
    }

    row.clickHandler(OptionItemLayout.onItemClick(context))
    return row.build()
  }

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(ClickEvent::class)
  fun onItemClick(context: ComponentContext, @Prop tagItem: TagItem) {
    tagItem.listener()
  }
}