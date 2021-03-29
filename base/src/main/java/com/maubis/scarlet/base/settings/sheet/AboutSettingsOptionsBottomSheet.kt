package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.main.sheets.WhatsNewBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.openSheet

class AboutSettingsOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.home_option_about

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
      title = R.string.home_option_about_page,
      subtitle = R.string.home_option_about_page_subtitle,
      icon = R.drawable.ic_info,
      listener = {
        openSheet(activity, AboutUsBottomSheet())
        dismiss()
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.whats_new_title,
      subtitle = R.string.whats_new_subtitle,
      icon = R.drawable.ic_whats_new,
      listener = {
        openSheet(activity, WhatsNewBottomSheet())
        dismiss()
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.internal_settings_title,
      subtitle = R.string.internal_settings_description,
      icon = R.drawable.icon_code_block,
      listener = {
        openSheet(activity, InternalSettingsOptionsBottomSheet())
        dismiss()
      }
    ))
    return options
  }
}