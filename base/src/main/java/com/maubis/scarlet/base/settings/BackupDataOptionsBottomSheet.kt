package com.maubis.scarlet.base.settings

import android.app.Dialog
import android.content.Intent
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.backup.PermissionUtils
import com.maubis.scarlet.base.backup.ui.ExportNotesBottomSheet
import com.maubis.scarlet.base.backup.ui.ImportNoteActivity
import com.maubis.scarlet.base.backup.ui.PermissionBottomSheet
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.security.PinLockController.isPinCodeEnabled
import com.maubis.scarlet.base.security.openUnlockSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.ui.ThemedActivity

class BackupDataOptionsBottomSheet : LithoOptionBottomSheet() {
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
        if (manager.hasAllPermissions()) {
          openExportSheet(activity)
          dismiss()
        } else {
          openSheet(activity, PermissionBottomSheet())
        }
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_import,
      subtitle = R.string.home_option_import_subtitle,
      icon = R.drawable.ic_import,
      listener = {
        val manager = PermissionUtils.getStoragePermissionManager(activity)
        if (manager.hasAllPermissions()) {
          activity.startActivity(Intent(activity, ImportNoteActivity::class.java))
          dismiss()
        } else {
          openSheet(activity, PermissionBottomSheet())
        }
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_delete_notes_and_more,
      subtitle = R.string.home_option_delete_notes_and_more_details,
      icon = R.drawable.ic_delete_permanently,
      listener = {
        openSheet(activity, DeleteAndMoreOptionsBottomSheet())
        dismiss()
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