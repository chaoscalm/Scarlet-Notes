package com.maubis.scarlet.base.settings

import android.app.Dialog
import android.content.Intent
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.backup.activity.ImportNoteActivity
import com.maubis.scarlet.base.backup.sheet.ExportNotesBottomSheet
import com.maubis.scarlet.base.backup.sheet.PermissionBottomSheet
import com.maubis.scarlet.base.backup.support.PermissionUtils
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.security.PinLockController.isPinCodeEnabled
import com.maubis.scarlet.base.security.openUnlockSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.ui.ThemedActivity

class BackupSettingsOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.home_option_backup_options

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
      title = R.string.home_option_export,
      subtitle = R.string.home_option_export_subtitle,
      icon = R.drawable.ic_export,
      listener = {
        val manager = PermissionUtils.getStoragePermissionManager(activity)
        val hasAllPermissions = manager.hasAllPermissions()
        when (hasAllPermissions) {
          true -> {
            openExportSheet(activity)
            dismiss()
          }
          false -> {
            openSheet(activity, PermissionBottomSheet())
          }
        }
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_import,
      subtitle = R.string.home_option_import_subtitle,
      icon = R.drawable.ic_import,
      listener = {
        val manager = PermissionUtils.getStoragePermissionManager(activity)
        val hasAllPermissions = manager.hasAllPermissions()
        when (hasAllPermissions) {
          true -> {
            activity.startActivity(Intent(activity, ImportNoteActivity::class.java))
            dismiss()
          }
          false -> {
            openSheet(activity, PermissionBottomSheet())
          }
        }
      }
    ))
    return options
  }

  private fun openExportSheet(activity: MainActivity) {
    if (!isPinCodeEnabled()) {
      openSheet(activity, ExportNotesBottomSheet())
      return
    }

    openUnlockSheet(
      activity = activity as ThemedActivity,
      onUnlockSuccess = { openSheet(activity, ExportNotesBottomSheet()) },
      onUnlockFailure = { openExportSheet(activity) })
  }
}