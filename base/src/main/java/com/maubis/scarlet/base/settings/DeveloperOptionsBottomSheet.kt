package com.maubis.scarlet.base.settings

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.common.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.common.sheets.LithoOptionsItem
import com.maubis.scarlet.base.home.MainActivity

class DeveloperOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.internal_settings_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(
      LithoOptionsItem(
        title = R.string.internal_settings_enable_fullscreen_title,
        subtitle = R.string.internal_settings_enable_fullscreen_description,
        icon = R.drawable.ic_staggered_grid,
        listener = {
          ScarletApp.prefs.enableFullscreen = !ScarletApp.prefs.enableFullscreen
          if (ScarletApp.prefs.enableFullscreen) {
            activity.enterFullScreen()
          }
          refresh(activity, dialog)
        },
        isSelectable = true,
        selected = ScarletApp.prefs.enableFullscreen
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.internal_settings_show_uuid_title,
        subtitle = R.string.internal_settings_show_uuid_description,
        icon = R.drawable.ic_code_inline,
        listener = {
          ScarletApp.prefs.showNotesUuids = !ScarletApp.prefs.showNotesUuids
          refresh(activity, dialog)
        },
        isSelectable = true,
        selected = ScarletApp.prefs.showNotesUuids
      ))
    return options
  }
}