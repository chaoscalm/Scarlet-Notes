package com.maubis.scarlet.base.settings

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.backup.NoteImporter
import com.maubis.scarlet.base.backup.ui.ExportNotesBottomSheet
import com.maubis.scarlet.base.common.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.common.sheets.LithoOptionsItem
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.security.PinLockController.isPinCodeEnabled
import com.maubis.scarlet.base.security.PincodeBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BackupDataOptionsBottomSheet : LithoOptionBottomSheet() {
  private val filePickerLauncher = registerForActivityResult(OpenDocument(), this::importBackupFromSelectedFile)

  override fun title(): Int = R.string.home_option_backup_options

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
      title = R.string.home_option_export,
      subtitle = R.string.home_option_export_subtitle,
      icon = R.drawable.ic_export,
      listener = {
        openExportSheet(activity)
        dismiss()
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_import,
      subtitle = R.string.home_option_import_subtitle,
      icon = R.drawable.ic_import,
      listener = {
        try {
          filePickerLauncher.launch(arrayOf("text/*", "application/json"))
        } catch (_: ActivityNotFoundException) {
          Toast.makeText(context, R.string.file_picker_missing, Toast.LENGTH_LONG).show()
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

  private fun importBackupFromSelectedFile(fileUri: Uri?) {
    if (fileUri == null)
      return

    lifecycleScope.launch(Dispatchers.IO) {
      requireContext().contentResolver.openInputStream(fileUri)?.use { stream ->
        val fileContent = stream.bufferedReader().use { it.readText() }
        tryPerformBackupImport(fileContent)
        withContext(Dispatchers.Main) { dismiss() }
      }
    }
  }

  private suspend fun tryPerformBackupImport(fileContent: String) {
    if (fileContent.isEmpty()) {
      showImportOutcome(R.string.notice_import_failed_empty_file)
      return
    }

    try {
      NoteImporter.importFromBackupContent(requireContext(), fileContent)
      showImportOutcome(R.string.notice_import_successful)
    } catch (e: Exception) {
      Log.e("Scarlet", "Backup import error", e)
      showImportOutcome(R.string.notice_import_completed_with_error)
    }
  }

  private suspend fun showImportOutcome(@StringRes messageTextRes: Int) {
    withContext(Dispatchers.Main) {
      Toast.makeText(requireContext(), messageTextRes, Toast.LENGTH_LONG).show()
      (activity as MainActivity).resetAndLoadData()
    }
  }

  private fun openExportSheet(activity: MainActivity) {
    if (!isPinCodeEnabled()) {
      openSheet(activity, ExportNotesBottomSheet())
      return
    }

    PincodeBottomSheet.openForUnlock(activity as ThemedActivity,
      onUnlockSuccess = { openSheet(activity, ExportNotesBottomSheet()) },
      onUnlockFailure = { openExportSheet(activity) })
  }
}