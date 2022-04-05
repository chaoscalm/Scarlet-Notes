package com.maubis.scarlet.base.backup

import android.content.Context
import com.google.gson.Gson
import com.maubis.scarlet.base.backup.data.ExportFileFormat

object NoteImporter {
  fun importFromBackupContent(context: Context, content: String) {
    val fileFormat = Gson().fromJson(content, ExportFileFormat::class.java)
    fileFormat.tags.forEach {
      it.saveIfNotPresent()
    }
    fileFormat.notes.forEach {
      it.saveIfNeeded(context)
    }
    fileFormat.folders?.forEach {
      it.saveIfNotPresent()
    }
  }
}