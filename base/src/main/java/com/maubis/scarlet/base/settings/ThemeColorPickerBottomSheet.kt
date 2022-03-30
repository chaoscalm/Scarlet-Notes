package com.maubis.scarlet.base.settings

import android.app.Dialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import com.facebook.litho.*
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.OptionItemLayout
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.specs.EmptySpec
import com.maubis.scarlet.base.support.specs.RoundIcon
import com.maubis.scarlet.base.support.ui.Theme
import com.maubis.scarlet.base.support.ui.ThemeManager.Companion.getThemeFromStore
import com.maubis.scarlet.base.support.ui.sThemeDarkenNoteColor
import com.maubis.scarlet.base.support.ui.sThemeIsAutomatic
import com.maubis.scarlet.base.support.ui.setThemeFromSystem
import com.maubis.scarlet.base.support.utils.OsVersionUtils

@LayoutSpec
object ThemeColorPickerItemSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop theme: Theme,
    @Prop isSelected: Boolean): Component {

    val icon = RoundIcon.create(context)
      .showBorder(true)
      .iconSizeDip(64f)
      .iconPaddingDip(16f)
      .onClick { }
      .flexGrow(1f)
      .isClickDisabled(true)
    when (isSelected) {
      true -> icon.iconRes(R.drawable.ic_done_white_48dp)
        .bgColorRes(R.color.colorAccent)
        .iconColor(Color.WHITE)
      false -> icon.iconRes(R.drawable.icon_realtime_markdown)
        .bgColorRes(theme.background)
        .iconColorRes(theme.primaryText)
    }
    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .child(icon)
    row.clickHandler(ThemeColorPickerItem.onItemClick(context))
    return row.build()
  }

  @Suppress("UNUSED_PARAMETER")
  @OnEvent(ClickEvent::class)
  fun onItemClick(
          context: ComponentContext,
          @Prop theme: Theme,
          @Prop onThemeSelected: (Theme) -> Unit) {
    onThemeSelected(theme)
  }
}

class ThemeColorPickerBottomSheet : LithoBottomSheet() {

  var onThemeChange: (Theme) -> Unit = {}

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val column = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.theme_page_title)
          .marginDip(YogaEdge.HORIZONTAL, 0f))

    if (OsVersionUtils.canUseSystemTheme()) {
      column.child(
        OptionItemLayout.create(componentContext)
          .option(
            LithoOptionsItem(
              title = R.string.theme_use_system_theme,
              subtitle = R.string.theme_use_system_theme_details,
              icon = R.drawable.ic_action_color,
              listener = {},
              isSelectable = true,
              selected = sThemeIsAutomatic,
              actionIcon = 0
            ))
          .onClick {
            val context = componentContext.androidContext as AppCompatActivity
            sThemeIsAutomatic = !sThemeIsAutomatic
            if (sThemeIsAutomatic) {
              setThemeFromSystem(context)
              onThemeChange(appTheme.get())
            }
            refresh(context, dialog)
          })
    }

    if (appTheme.isNightTheme()) {
      column.child(
        OptionItemLayout.create(componentContext)
          .option(
            LithoOptionsItem(
              title = R.string.theme_dark_notes,
              subtitle = R.string.theme_dark_notes_details,
              icon = R.drawable.night_mode_white_48dp,
              listener = {},
              isSelectable = true,
              selected = sThemeDarkenNoteColor,
              actionIcon = 0
            ))
          .onClick {
            val activity = componentContext.androidContext as AppCompatActivity
            sThemeDarkenNoteColor = !sThemeDarkenNoteColor
            activity.recreate()
          })
    }

    if (!sThemeIsAutomatic) {
      var flex: Row.Builder? = null
      Theme.values().forEachIndexed { index, theme ->
        if (index % 4 == 0) {
          column.child(flex)
          flex = Row.create(componentContext)
            .widthPercent(100f)
            .alignItems(YogaAlign.CENTER)
            .paddingDip(YogaEdge.VERTICAL, 12f)
        }

        flex?.child(
          ThemeColorPickerItem.create(componentContext)
            .theme(theme)
            .isSelected(theme.name == getThemeFromStore().name)
            .onThemeSelected { newTheme ->
              onThemeChange(newTheme)
            }
            .flexGrow(1f))
      }
      column.child(flex)
    }

    column.child(EmptySpec.create(componentContext).widthPercent(100f).heightDip(24f))
    column.child(BottomSheetBar.create(componentContext)
                   .primaryActionRes(R.string.import_export_layout_exporting_done)
                   .onPrimaryClick {
                     dismiss()
                   }.paddingDip(YogaEdge.VERTICAL, 8f))
    return column.build()
  }
}
