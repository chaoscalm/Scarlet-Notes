package com.maubis.scarlet.base.settings

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.common.sheets.*
import com.maubis.scarlet.base.home.MainActivity

class UIOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.home_option_ui_experience

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = componentContext.androidContext as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
      title = R.string.home_option_theme_color,
      subtitle = R.string.home_option_theme_color_subtitle,
      icon = R.drawable.ic_day_night,
      listener = {
          openSheet(activity, ThemeColorPickerBottomSheet().apply {
              this.onThemeChange = { theme ->
                  if (ScarletApp.prefs.selectedTheme != theme.name) {
                      ScarletApp.prefs.selectedTheme = theme.name
                      appTheme.notifyChange(activity)
                      activity.recreate()
                  }
              }
          })
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.note_option_default_color,
      subtitle = R.string.note_option_default_color_subtitle,
      icon = R.drawable.ic_color_picker,
      listener = {
        val config = ColorPickerDefaultController(
          title = R.string.note_option_default_color,
          colors = listOf(
            activity.resources.getIntArray(R.array.bright_colors),
            activity.resources.getIntArray(R.array.bright_colors_accent)),
          selectedColor = ScarletApp.prefs.noteDefaultColor,
          onColorSelected = { ScarletApp.prefs.noteDefaultColor = it }
        )
        openSheet(activity, ColorPickerBottomSheet().apply { this.config = config })
      }
    ))
    val isTablet = resources.getBoolean(R.bool.is_tablet)
    options.add(
      LithoOptionsItem(
        title = R.string.home_option_enable_list_view,
        subtitle = R.string.home_option_enable_list_view_subtitle,
        icon = R.drawable.ic_list_layout,
        listener = {
          ScarletApp.prefs.displayNotesListAsGrid = false
          activity.notifyAdapterExtraChanged()
          refresh(activity, dialog)
        },
        visible = !isTablet && ScarletApp.prefs.displayNotesListAsGrid
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.home_option_enable_grid_view,
        subtitle = R.string.home_option_enable_grid_view_subtitle,
        icon = R.drawable.ic_staggered_grid,
        listener = {
          ScarletApp.prefs.displayNotesListAsGrid = true
          activity.notifyAdapterExtraChanged()
          refresh(activity, dialog)
        },
        visible = !isTablet && !ScarletApp.prefs.displayNotesListAsGrid
      ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_order_notes,
      subtitle = ScarletApp.prefs.notesSortingTechnique.label,
      icon = R.drawable.ic_sort,
      listener = {
        SortingOptionsBottomSheet.openSheet(activity)
        refresh(activity, dialog)
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.note_option_number_lines,
      subtitle = 0,
      content = activity.getString(R.string.note_option_number_lines_subtitle, ScarletApp.prefs.notePreviewLines),
      icon = R.drawable.ic_text_content,
      listener = {
        openSheet(activity, LineCountBottomSheet())
      }
    ))
    return options
  }
}