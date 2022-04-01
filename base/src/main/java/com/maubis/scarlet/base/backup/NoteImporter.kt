package com.maubis.scarlet.base.backup

import android.content.Context
import com.google.gson.Gson
import com.maubis.scarlet.base.backup.data.ExportFileFormat
import com.maubis.scarlet.base.common.utils.logNonCriticalError
import com.maubis.scarlet.base.note.NoteBuilder

object NoteImporter {
  fun importFromBackupContent(context: Context, content: String) {
    try {
        val fileFormat = Gson().fromJson(content, ExportFileFormat::class.java)
        if (fileFormat == null) {
          importNoteFallback(content, context)
          return
        }
        fileFormat.tags.forEach {
          it.saveIfNotPresent()
        }
        fileFormat.notes.forEach {
          it.saveIfNeeded(context)
        }
        fileFormat.folders?.forEach {
          it.saveIfNotPresent()
        }
    } catch (exception: Exception) {
      importNoteFallback(content, context)
      logNonCriticalError(exception)
    }
  }

  private fun importNoteFallback(content: String, context: Context) {
    content
      .split(EXPORT_NOTE_SEPARATOR)
      .map {
        it.trim()
      }
      .filter { it.isNotBlank() }
      .forEach {
        NoteBuilder.gen("", it).save(context)
      }
  }
}