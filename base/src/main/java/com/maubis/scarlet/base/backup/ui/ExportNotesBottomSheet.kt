package com.maubis.scarlet.base.backup.ui

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.core.content.edit
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

const val STORE_KEY_BACKUP_MARKDOWN = "KEY_BACKUP_MARKDOWN"
var sBackupMarkdown: Boolean
  get() = ScarletApp.appPreferences.getBoolean(STORE_KEY_BACKUP_MARKDOWN, false)
  set(value) = ScarletApp.appPreferences.edit { putBoolean(STORE_KEY_BACKUP_MARKDOWN, value) }

const val STORE_KEY_BACKUP_LOCKED = "KEY_BACKUP_LOCKED"
var sBackupLockedNotes: Boolean
  get() = ScarletApp.appPreferences.getBoolean(STORE_KEY_BACKUP_LOCKED, true)
  set(value) = ScarletApp.appPreferences.edit { putBoolean(STORE_KEY_BACKUP_LOCKED, value) }

const val STORE_KEY_AUTO_BACKUP_MODE = "KEY_AUTO_BACKUP_MODE"
var sAutoBackupMode: Boolean
  get() = ScarletApp.appPreferences.getBoolean(STORE_KEY_AUTO_BACKUP_MODE, false)
  set(value) = ScarletApp.appPreferences.edit { putBoolean(STORE_KEY_AUTO_BACKUP_MODE, value) }

class ExportNotesBottomSheet : LithoBottomSheet() {
  private val saveFileLauncher = registerForActivityResult(CreateDocument(), this::performManualBackup)

  override fun isAlwaysExpanded(): Boolean = true

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
            saveFileLauncher.launch(NoteExporter.getDefaultManualBackupFileName())
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
        icon = R.drawable.ic_markdown_logo,
        listener = { sBackupMarkdown = !sBackupMarkdown },
        isSelectable = true,
        selected = sBackupMarkdown
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.import_export_locked,
        subtitle = R.string.import_export_locked_details,
        icon = R.drawable.ic_action_lock,
        listener = { sBackupLockedNotes = !sBackupLockedNotes },
        isSelectable = true,
        selected = sBackupLockedNotes
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.home_option_auto_export,
        subtitle = R.string.home_option_auto_export_subtitle,
        icon = R.drawable.ic_time,
        listener = {
          val manager = PermissionUtils.getStoragePermissionManager(activity)
          val hasAllPermissions = manager.hasAllPermissions()
          when {
            sAutoBackupMode -> {
              sAutoBackupMode = false
            }
            hasAllPermissions -> {
              sAutoBackupMode = true
            }
            else -> openSheet(activity, PermissionBottomSheet())
          }
        },
        isSelectable = true,
        selected = sAutoBackupMode
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