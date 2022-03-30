package com.maubis.scarlet.base.settings

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.BuildConfig
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.openSheet

class SettingsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.home_option_sheet_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoOptionsItem>()

    options.add(LithoOptionsItem(
      title = R.string.home_option_ui_experience,
      subtitle = R.string.home_option_ui_experience_subtitle,
      icon = R.drawable.ic_action_grid,
      listener = {
        openSheet(activity, UIOptionsBottomSheet())
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_editor_options_title,
      subtitle = R.string.home_option_editor_options_description,
      icon = R.drawable.ic_edit_white_48dp,
      listener = {
        openSheet(activity, EditorOptionsBottomSheet())
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_backup_options,
      subtitle = R.string.home_option_backup_options_subtitle,
      icon = R.drawable.ic_export,
      listener = {
        openSheet(activity, BackupDataOptionsBottomSheet())
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_security,
      subtitle = R.string.home_option_security_subtitle,
      icon = R.drawable.ic_option_security,
      listener = {
        openSheet(activity, SecurityOptionsBottomSheet())
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_widget_options_title,
      subtitle = R.string.home_option_widget_options_description,
      icon = R.drawable.icon_widget,
      listener = {
        openSheet(activity, WidgetOptionsBottomSheet())
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_about,
      subtitle = R.string.home_option_about_subtitle,
      icon = R.drawable.ic_info,
      listener = {
        openSheet(activity, AboutBottomSheet())
      }
    ))
    if (BuildConfig.DEBUG) {
      options.add(LithoOptionsItem(
        title = R.string.internal_settings_title,
        subtitle = R.string.internal_settings_description,
        icon = R.drawable.icon_code_block,
        listener = {
          openSheet(activity, DeveloperOptionsBottomSheet())
        }
      ))
    }
    return options
  }

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = SettingsBottomSheet()

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}