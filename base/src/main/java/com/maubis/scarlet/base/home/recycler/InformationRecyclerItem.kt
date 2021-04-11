package com.maubis.scarlet.base.home.recycler

import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.export.sheet.BackupSettingsOptionsBottomSheet
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.settings.UISettingsOptionsBottomSheet
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.sheets.openSheet
import java.util.*

const val KEY_THEME_OPTIONS = "KEY_THEME_OPTIONS"
const val KEY_BACKUP_OPTIONS = "KEY_BACKUP_OPTIONS"

class InformationRecyclerItem(val icon: Int, val title: Int, val source: Int, val function: () -> Unit) : RecyclerItem() {
  override val type = Type.INFORMATION
}

fun probability(probability: Float): Boolean = Random().nextFloat() <= probability

fun shouldShowThemeInformationItem(): Boolean {
  return probability(0.01f)
    && !appPreferences.get(KEY_THEME_OPTIONS, false)
}

fun getThemeInformationItem(activity: MainActivity): InformationRecyclerItem {
  return InformationRecyclerItem(
    R.drawable.ic_action_grid,
    R.string.home_option_ui_experience,
    R.string.home_option_ui_experience_subtitle
  ) {
    appPreferences.put(KEY_THEME_OPTIONS, true)
    openSheet(activity, UISettingsOptionsBottomSheet())
  }
}

fun shouldShowBackupInformationItem(): Boolean {
  return probability(0.01f)
    && !appPreferences.get(KEY_BACKUP_OPTIONS, false)
}

fun getBackupInformationItem(activity: MainActivity): InformationRecyclerItem {
  return InformationRecyclerItem(
    R.drawable.ic_export,
    R.string.home_option_backup_options,
    R.string.home_option_backup_options_subtitle
  ) {
    appPreferences.put(KEY_BACKUP_OPTIONS, true)
    openSheet(activity, BackupSettingsOptionsBottomSheet())
  }
}