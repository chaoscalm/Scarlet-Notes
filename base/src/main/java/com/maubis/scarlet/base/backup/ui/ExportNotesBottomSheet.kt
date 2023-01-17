package com.maubis.scarlet.base.backup.ui

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.lifecycle.lifecycleScope
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.backup.NoteExporter
import com.maubis.scarlet.base.backup.PermissionUtils
import com.maubis.scarlet.base.common.sheets.*
import com.maubis.scarlet.base.common.specs.BottomSheetBar
import com.maubis.scarlet.base.home.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExportNotesBottomSheet : LithoBottomSheet() {
  private val saveFileLauncher = registerForActivityResult(CreateDocument("text/plain"), this::performManualBackup)

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val component = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.import_export_layout_exporting)
          .paddingDip(YogaEdge.HORIZONTAL, 20f)
          .marginDip(YogaEdge.HORIZONTAL, 0f))

    getOptions(componentContext).forEach {
      if (it.visible) {
        component.child(OptionItemLayout.create(componentContext)
                          .option(it)
                          .onClick {
                            it.listener()
                            refresh(componentContext.androidContext, dialog)
                          })
      }
    }

    component.child(
      BottomSheetBar
        .create(componentContext)
        .primaryActionRes(R.string.import_export_layout_export_action)
        .onPrimaryClick {
          try {
            val suggestedFileName = if (ScarletApp.prefs.backupInMarkdown) {
              NoteExporter.getDefaultMarkdownExportFileName()
            } else {
              NoteExporter.getDefaultManualBackupFileName()
            }
            saveFileLauncher.launch(suggestedFileName)
          } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.file_picker_missing, Toast.LENGTH_LONG).show()
          }
        }
        .paddingDip(YogaEdge.HORIZONTAL, 20f)
        .paddingDip(YogaEdge.VERTICAL, 8f)
    )
    return component.build()
  }

  fun getOptions(componentContext: ComponentContext): List<LithoOptionsItem> {
    val activity = componentContext.androidContext as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(
      LithoOptionsItem(
        title = R.string.home_option_export_markdown,
        subtitle = R.string.home_option_export_markdown_subtitle,
        icon = R.drawable.ic_markdown,
        listener = { ScarletApp.prefs.backupInMarkdown = !ScarletApp.prefs.backupInMarkdown },
        isSelectable = true,
        selected = ScarletApp.prefs.backupInMarkdown
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.import_export_locked,
        subtitle = R.string.import_export_locked_details,
        icon = R.drawable.ic_lock,
        listener = { ScarletApp.prefs.backupLockedNotes = !ScarletApp.prefs.backupLockedNotes },
        isSelectable = true,
        selected = ScarletApp.prefs.backupLockedNotes
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.home_option_auto_export,
        subtitle = R.string.home_option_auto_export_subtitle,
        icon = R.drawable.ic_time,
        listener = {
          val hasRequiredPermissions = PermissionUtils.hasExternalStorageAccess(requireContext())
          when {
            ScarletApp.prefs.performAutomaticBackups -> ScarletApp.prefs.performAutomaticBackups = false
            hasRequiredPermissions -> ScarletApp.prefs.performAutomaticBackups = true
            else -> openSheet(activity, StoragePermissionBottomSheet())
          }
        },
        isSelectable = true,
        selected = ScarletApp.prefs.performAutomaticBackups
      ))
    return options
  }

  private fun performManualBackup(uri: Uri?) {
    if (uri == null)
      return

    lifecycleScope.launch {
      try {
        withContext(Dispatchers.IO) { exportNotesToUri(uri) }
        Toast.makeText(context, R.string.import_export_layout_exported, Toast.LENGTH_SHORT).show()
      } catch (e: Exception) {
        Log.e("Scarlet", "Backup export error", e)
        Toast.makeText(context, R.string.import_export_layout_export_failed, Toast.LENGTH_SHORT).show()
      }
      dismiss()
    }
  }

  private fun exportNotesToUri(uri: Uri) {
    val descriptor = requireContext().contentResolver.openFileDescriptor(uri, "w")
      ?: throw IllegalStateException("Could not open backup file in write mode")
    descriptor.use { NoteExporter.exportNotesToFile(it.fileDescriptor) }
  }
}