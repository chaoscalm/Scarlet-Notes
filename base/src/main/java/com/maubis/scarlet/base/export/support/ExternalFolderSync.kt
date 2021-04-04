package com.maubis.scarlet.base.export.support

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.appPreferences
import com.maubis.scarlet.base.config.ApplicationBase.Companion.folderSync
import com.maubis.scarlet.base.export.data.ExportableFolder
import com.maubis.scarlet.base.export.data.ExportableNote
import com.maubis.scarlet.base.export.data.ExportableTag
import com.maubis.scarlet.base.export.remote.FolderRemoteDatabase
import com.maubis.scarlet.base.export.sheet.NOTES_EXPORT_FOLDER
import com.maubis.scarlet.base.support.utils.OsVersionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

const val KEY_EXTERNAL_FOLDER_SYNC_ENABLED = "external_folder_sync_enabled"
const val KEY_EXTERNAL_FOLDER_SYNC_LAST_SCAN = "external_folder_sync_last_sync"
const val KEY_EXTERNAL_FOLDER_SYNC_BACKUP_LOCKED = "external_folder_sync_backup_locked"
const val KEY_EXTERNAL_FOLDER_SYNC_PATH = "external_folder_sync_path"

var sExternalFolderSync: Boolean
  get() = appPreferences.get(KEY_EXTERNAL_FOLDER_SYNC_ENABLED, false)
  set(value) = appPreferences.put(KEY_EXTERNAL_FOLDER_SYNC_ENABLED, value)

var sFolderSyncPath: String
  get() = appPreferences.get(KEY_EXTERNAL_FOLDER_SYNC_PATH, "$NOTES_EXPORT_FOLDER/Sync/")
  set(value) = appPreferences.put(KEY_EXTERNAL_FOLDER_SYNC_PATH, value)

var sFolderSyncBackupLocked: Boolean
  get() = appPreferences.get(KEY_EXTERNAL_FOLDER_SYNC_BACKUP_LOCKED, true)
  set(value) = appPreferences.put(KEY_EXTERNAL_FOLDER_SYNC_BACKUP_LOCKED, value)

object ExternalFolderSync {

  fun hasPermission(context: Context): Boolean {
    return !(OsVersionUtils.requiresPermissions()
      && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
  }

  fun enable(context: Context, enabled: Boolean) {
    if (enabled) {
      if (!hasPermission(context)) {
        GlobalScope.launch(Dispatchers.Main) {
          Toast.makeText(context, R.string.permission_layout_give_permission_details, Toast.LENGTH_SHORT).show()
          folderSync?.reset()
        }
        return
      }
      sExternalFolderSync = true
      loadFirstTime()
    } else {
      sExternalFolderSync = false
      folderSync?.reset()
    }
  }

  fun loadFirstTime() {
    folderSync?.init(
      {
        ApplicationBase.instance.notesRepository.getAll().forEach {
          folderSync?.insert(ExportableNote(it))
        }
      },
      {
        ApplicationBase.instance.tagsRepository.getAll().forEach {
          folderSync?.insert(ExportableTag(it))
        }
      },
      {
        ApplicationBase.instance.foldersRepository.getAll().forEach {
          folderSync?.insert(ExportableFolder(it))
        }
      })
  }

  fun setup(context: Context) {
    if (!sExternalFolderSync) {
      return
    }

    if (!hasPermission(context)) {
      sExternalFolderSync = false
      return
    }
    folderSync = FolderRemoteDatabase(WeakReference(context))
    folderSync?.init()
  }
}