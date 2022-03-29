package com.maubis.scarlet.base.settings

import android.app.Dialog
import androidx.core.content.edit
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem

const val KEY_INTERNAL_ENABLE_FULL_SCREEN = "internal_enable_full_screen"
var sInternalEnableFullScreen: Boolean
  get() = appPreferences.getBoolean(KEY_INTERNAL_ENABLE_FULL_SCREEN, false)
  set(value) = appPreferences.edit { putBoolean(KEY_INTERNAL_ENABLE_FULL_SCREEN, value) }

const val KEY_INTERNAL_SHOW_UUID = "internal_show_uuid"
var sInternalShowUUID: Boolean
  get() = appPreferences.getBoolean(KEY_INTERNAL_SHOW_UUID, false)
  set(value) = appPreferences.edit { putBoolean(KEY_INTERNAL_SHOW_UUID, value) }

class InternalSettingsOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.internal_settings_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(
      LithoOptionsItem(
        title = R.string.internal_settings_enable_fullscreen_title,
        subtitle = R.string.internal_settings_enable_fullscreen_description,
        icon = R.drawable.ic_action_grid,
        listener = {
          sInternalEnableFullScreen = !sInternalEnableFullScreen
          reset(activity, dialog)
        },
        isSelectable = true,
        selected = sInternalEnableFullScreen
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.internal_settings_show_uuid_title,
        subtitle = R.string.internal_settings_show_uuid_description,
        icon = R.drawable.ic_code_white_48dp,
        listener = {
          sInternalShowUUID = !sInternalShowUUID
          reset(activity, dialog)
        },
        isSelectable = true,
        selected = sInternalShowUUID
      ))
    return options
  }
}