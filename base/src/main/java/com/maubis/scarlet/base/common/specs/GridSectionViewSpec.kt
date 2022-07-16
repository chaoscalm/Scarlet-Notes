package com.maubis.scarlet.base.common.specs

import android.graphics.Color
import android.text.Layout
import android.text.TextUtils
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.SolidColor
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.common.ui.ThemeColor

data class GridSectionItem(
  val sectionColor: Int = 0,
  val options: List<GridSectionOptionItem>)

data class GridSectionOptionItem(
  val icon: Int,
  val label: Int,
  val listener: () -> Unit,
  val visible: Boolean = true)

@LayoutSpec
object GridOptionSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop option: GridSectionOptionItem,
    @Prop solidSectionColor: Boolean,
    @Prop(resType = ResType.COLOR) labelColor: Int,
    @Prop(resType = ResType.COLOR) iconColor: Int,
    @Prop(resType = ResType.DIMEN_SIZE) iconSize: Int,
    @Prop(resType = ResType.COLOR) sectionColor: Int): Component {
    return Column.create(context)
      .alignItems(YogaAlign.CENTER)
      .alignContent(YogaAlign.CENTER)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 4f)
      .child(
        RoundIcon.create(context)
          .bgColor(sectionColor)
          .iconColor(iconColor)
          .iconRes(option.icon)
          .iconSizePx(iconSize)
          .iconPaddingRes(R.dimen.primary_round_icon_padding)
          .bgAlpha(if (solidSectionColor) 255 else 15)
      )
      .child(
        Text.create(context)
          .textRes(option.label)
          .textAlignment(Layout.Alignment.ALIGN_CENTER)
          .typeface(appTypeface.title())
          .textSizeRes(R.dimen.font_size_small)
          .paddingDip(YogaEdge.VERTICAL, 8f)
          .paddingDip(YogaEdge.HORIZONTAL, 12f)
          .minLines(1)
          .maxLines(2)
          .ellipsize(TextUtils.TruncateAt.END)
          .textColor(labelColor))
      .clickHandler(GridOption.onClick(context))
      .build()
  }

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(ClickEvent::class)
  fun onClick(context: ComponentContext, @Prop option: GridSectionOptionItem) {
    option.listener()
  }
}

@LayoutSpec
object GridSectionViewSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop section: GridSectionItem,
    @Prop(resType = ResType.DIMEN_SIZE) iconSize: Int,
    @Prop(optional = true) numColumns: Int?,
    @Prop(optional = true) showSeparator: Boolean?): Component {
    val column = Column.create(context)
    val primaryColor = appTheme.getColor(ThemeColor.SECONDARY_TEXT)
    val iconColor = appTheme.getColor(ThemeColor.ICON)

    val visibleOptions = section.options.filter { it.visible }
    val getComponentAtIndex: (Int) -> Component = { index ->
      when {
        index >= visibleOptions.size -> EmptySpec.create(context)
          .flexGrow(1f)
          .flexBasisDip(1f)
          .build()
        else -> GridOption.create(context)
          .flexGrow(1f)
          .flexBasisDip(1f)
          .solidSectionColor(section.sectionColor != 0)
          .labelColor(primaryColor)
          .iconSizePx(iconSize)
          .iconColor(if (section.sectionColor == 0) iconColor else Color.WHITE)
          .sectionColor(if (section.sectionColor == 0) primaryColor else section.sectionColor)
          .option(visibleOptions[index])
          .build()
      }
    }

    val numberOfColumns = numColumns ?: 3
    var index = 0
    while (true) {
      val row = Row.create(context)
        .widthPercent(100f)
      if (index >= visibleOptions.size) {
        break
      }

      for (delta in 0 until numberOfColumns) {
        row.child(getComponentAtIndex(index))
        index += 1
      }
      column.child(row)
    }

    if (showSeparator == true) {
      column.child(
        SolidColor.create(context)
          .color(appTheme.getColor(ThemeColor.PRIMARY_TEXT))
          .heightDip(1.5f)
          .widthDip(196f)
          .alignSelf(YogaAlign.CENTER)
          .marginDip(YogaEdge.VERTICAL, 12f)
          .alpha(0.1f))
    }
    return column.build()
  }
}