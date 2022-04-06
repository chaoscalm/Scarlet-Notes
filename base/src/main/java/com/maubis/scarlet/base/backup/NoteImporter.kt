package com.maubis.scarlet.base.backup

import android.content.Context
import com.google.gson.Gson
import com.maubis.scarlet.base.backup.data.ExportFileFormat
import java.util.*

object NoteImporter {
  fun importFromBackupContent(context: Context, content: String) {
    val backupData = Gson().fromJson(content, ExportFileFormat::class.java)
    fixInvalidFolderUuids(backupData)
    fixInvalidTagUuids(backupData)
    removeInvalidUuidsFromNotes(backupData)
    backupData.folders.forEach { it.saveIfNeeded() }
    backupData.tags.forEach { it.saveIfNotPresent() }
    backupData.notes.forEach { it.saveIfNeeded(context) }
  }

  // In backups made by upstream Scarlet Notes, folders and tags are given random strings
  // as UUIDs instead of "real" valid UUIDs
  private fun fixInvalidFolderUuids(backupData: ExportFileFormat) {
    backupData.folders.forEach { folder ->
      if (isInvalidUuid(folder.uuid)) {
        val newFolderUuid = UUID.randomUUID().toString()
        backupData.notes.forEach { note ->
          if (note.folder == folder.uuid)
            note.folder = newFolderUuid
        }
        folder.uuid = newFolderUuid
      }
    }
  }

  private fun fixInvalidTagUuids(backupData: ExportFileFormat) {
    backupData.tags.forEach { tag ->
      if (isInvalidUuid(tag.uuid)) {
        val newTagUuid = UUID.randomUUID().toString()
        backupData.notes.forEach { note ->
          if (note.tags.contains(tag.uuid))
            note.tags = note.tags.replace(tag.uuid, newTagUuid)
        }
        tag.uuid = newTagUuid
      }
    }
  }

  private fun removeInvalidUuidsFromNotes(backupData: ExportFileFormat) {
    backupData.notes.forEach { note ->
      if (note.folder !in backupData.folders.map { it.uuid }) {
        note.folder = ""
      }
      note.tagUuids().forEach { tagUuid ->
        if (tagUuid !in backupData.tags.map { it.uuid }) {
          note.tags = note.tags.replace(tagUuid, "")
        }
      }
    }
  }

  private fun isInvalidUuid(uuidString: String): Boolean {
    return runCatching { UUID.fromString(uuidString) }.isFailure
  }
}