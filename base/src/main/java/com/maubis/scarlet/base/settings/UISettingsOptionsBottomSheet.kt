package com.maubis.scarlet.base.settings

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.ScarletApp.Companion.appTheme
import com.maubis.scarlet.base.ScarletApp.Companion.appTypeface
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.settings.SortingOptionsBottomSheet.Companion.getSortingState
import com.maubis.scarlet.base.settings.SortingOptionsBottomSheet.Companion.getSortingTechniqueLabel
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.ui.font.sPreferenceTypeface
import com.maubis.scarlet.base.support.ui.sThemeLabel

var sUIUseGridView: Boolean
  get() = appPreferences.get("KEY_LIST_VIEW", true)
  set(isGrid) = appPreferences.put("KEY_LIST_VIEW", isGrid)

var sUIUseNoteColorAsBackground: Boolean
  get() = appPreferences.get("KEY_NOTE_VIEWER_BG_COLOR", false)
  set(value) = appPreferences.put("KEY_NOTE_VIEWER_BG_COLOR", value)

var sUIMarkdownEnabledOnHome: Boolean
  get() = appPreferences.get("KEY_MARKDOWN_HOME_ENABLED", true)
  set(value) = appPreferences.put("KEY_MARKDOWN_HOME_ENABLED", value)

class UISettingsOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.home_option_ui_experience

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = componentContext.androidContext as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
      title = R.string.home_option_theme_color,
      subtitle = R.string.home_option_theme_color_subtitle,
      icon = if (appTheme.isNightTheme()) R.drawable.night_mode_white_48dp else R.drawable.ic_action_day_mode,
      listener = {
          openSheet(activity, ThemeColorPickerBottomSheet().apply {
              this.onThemeChange = { theme ->
                  if (sThemeLabel != theme.name) {
                      sThemeLabel = theme.name
                      appTheme.notifyChange(activity)
                      activity.recreate()
                  }
              }
          })
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_typeface,
      subtitle = R.string.home_option_typeface_subtitle,
      icon = R.drawable.icon_typeface,
      listener = {
          openSheet(activity, TypefacePickerBottomSheet().apply {
              this.onTypefaceChange = { typeface ->
                  if (sPreferenceTypeface != typeface.name) {
                      sPreferenceTypeface = typeface.name
                      appTypeface.notifyChange(activity)
                      activity.recreate()
                  }
              }
          })
      }
    ))
    val isTablet = resources.getBoolean(R.bool.is_tablet)
    options.add(
      LithoOptionsItem(
        title = R.string.home_option_enable_list_view,
        subtitle = R.string.home_option_enable_list_view_subtitle,
        icon = R.drawable.ic_action_list,
        listener = {
          sUIUseGridView = false
          activity.notifyAdapterExtraChanged()
          reset(activity, dialog)
        },
        visible = !isTablet && sUIUseGridView
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.home_option_enable_grid_view,
        subtitle = R.string.home_option_enable_grid_view_subtitle,
        icon = R.drawable.ic_action_grid,
        listener = {
          sUIUseGridView = true
          activity.notifyAdapterExtraChanged()
          reset(activity, dialog)
        },
        visible = !isTablet && !sUIUseGridView
      ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_order_notes,
      subtitle = getSortingTechniqueLabel(getSortingState()),
      icon = R.drawable.ic_sort,
      listener = {
        SortingOptionsBottomSheet.openSheet(activity, { activity.refreshItems() })
        reset(activity, dialog)
      }
    ))
    options.add(
      LithoOptionsItem(
        title = R.string.note_option_font_size,
        subtitle = 0,
        content = activity.getString(R.string.note_option_font_size_subtitle, sEditorTextSize),
        icon = R.drawable.ic_title_white_48dp,
        listener = {
            openSheet(activity, FontSizeBottomSheet())
            reset(activity, dialog)
        },
        actionIcon = 0
      ))
    options.add(LithoOptionsItem(
      title = R.string.note_option_number_lines,
      subtitle = 0,
      content = activity.getString(R.string.note_option_number_lines_subtitle, sNoteItemLineCount),
      icon = R.drawable.ic_action_list,
      listener = {
        openSheet(activity, LineCountBottomSheet())
      }
    ))
    options.add(
      LithoOptionsItem(
        title = R.string.ui_options_note_background_color,
        subtitle = when (sUIUseNoteColorAsBackground) {
          true -> R.string.ui_options_note_background_color_settings_note
          false -> R.string.ui_options_note_background_color_settings_theme
        },
        icon = R.drawable.ic_action_color,
        listener = {
          sUIUseNoteColorAsBackground = !sUIUseNoteColorAsBackground
          reset(activity, dialog)
        },
        actionIcon = 0
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.markdown_sheet_home_markdown_support,
        subtitle = R.string.markdown_sheet_home_markdown_support_subtitle,
        icon = R.drawable.ic_markdown_logo,
        listener = {
          sUIMarkdownEnabledOnHome = !sUIMarkdownEnabledOnHome
          reset(activity, dialog)
        },
        isSelectable = true,
        selected = sUIMarkdownEnabledOnHome
      ))
    return options
  }
}