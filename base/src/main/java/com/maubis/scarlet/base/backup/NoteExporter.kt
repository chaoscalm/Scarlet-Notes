package com.maubis.scarlet.base.backup

import android.os.Environment
import androidx.core.content.edit
import com.google.gson.Gson
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.backup.data.ExportFileFormat
import com.maubis.scarlet.base.backup.data.ExportedFolder
import com.maubis.scarlet.base.backup.data.ExportedNote
import com.maubis.scarlet.base.backup.data.ExportedTag
import com.maubis.scarlet.base.backup.ui.sAutoBackupMode
import com.maubis.scarlet.base.backup.ui.sBackupLockedNotes
import com.maubis.scarlet.base.backup.ui.sBackupMarkdown
import com.maubis.scarlet.base.common.utils.formatTimestamp
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.editor.formats.FormatType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream

const val KEY_AUTO_BACKUP_LAST_TIMESTAMP = "KEY_AUTO_BACKUP_LAST_TIMESTAMP"

const val EXPORT_NOTE_SEPARATOR = ">S>C>A>R>L>E>T>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>N>O>T>E>S>"
const val EXPORT_VERSION = 6

const val MANUAL_BACKUP_FILENAME = "manual_backup"
const val AUTO_BACKUP_FOLDER = "ScarletNotes"
const val AUTO_BACKUP_FILENAME = "auto_backup"
const val AUTO_BACKUP_INTERVAL_MS = 1000 * 60 * 60 * 6 // 6 hours update

object NoteExporter {
  fun tryAutoExport() {
    GlobalScope.launch(Dispatchers.IO) {
      if (!sAutoBackupMode) {
        return@launch
      }
      val lastBackup = appPreferences.getLong(KEY_AUTO_BACKUP_LAST_TIMESTAMP, 0L)
      val lastTimestamp = data.notes.getLastTimestamp()
      if (lastBackup + AUTO_BACKUP_INTERVAL_MS >= lastTimestamp) {
        return@launch
      }

      val exportFile = getFileForAutomaticExport()
      if (exportFile === null) {
        return@launch
      }
      exportFile.writeText(getBackupFileContent())
      appPreferences.edit { putLong(KEY_AUTO_BACKUP_LAST_TIMESTAMP, System.currentTimeMillis()) }
    }
  }

  fun getDefaultManualBackupFileName(): String {
    return "$MANUAL_BACKUP_FILENAME ${getFormattedDateWithTime()}.txt"
  }

  fun exportNotesToFile(file: FileDescriptor) {
    val backupContent = getBackupFileContent()
    FileOutputStream(file).use { it.write(backupContent.toByteArray()) }
  }

  private fun getBackupFileContent(): String {
    val notesToBeExported = data.notes.getAll()
      .filter { sBackupLockedNotes || !it.locked }
      .filter { !it.excludeFromBackup }

    if (sBackupMarkdown) {
      return getMarkdownBackupFileContent(notesToBeExported)
    }

    val notes = notesToBeExported.map { ExportedNote(it) }
    val tags = data.tags.getAll().map { ExportedTag(it) }
    val folders = data.folders.getAll().map { ExportedFolder(it) }
    val fileContent = ExportFileFormat(EXPORT_VERSION, notes, tags, folders)
    return Gson().toJson(fileContent)
  }

  private fun getMarkdownBackupFileContent(notesToBeExported: List<Note>): String {
    var fileContent = "$EXPORT_NOTE_SEPARATOR\n\n"
    notesToBeExported
      .map { it.toExportedMarkdown() }
      .forEach {
        fileContent += it
        fileContent += "\n\n$EXPORT_NOTE_SEPARATOR\n\n"
      }
    return fileContent
  }

  /**
   * Converts the note's internal content format into markdown which can be used to export.
   */
  private fun Note.toExportedMarkdown(): String {
    val markdownBuilder = StringBuilder()
    getFormats().forEach { format ->
      val text = format.text
      val formatMarkdown = when (format.type) {
        FormatType.NUMBERED_LIST -> "- $text"
        FormatType.HEADING -> "# $text"
        FormatType.HEADING_3 -> "### $text"
        FormatType.BULLET_1 -> "- $text"
        FormatType.BULLET_2 -> "  - $text"
        FormatType.BULLET_3 -> "    - $text"
        FormatType.CHECKLIST_CHECKED -> "[x] $text"
        FormatType.CHECKLIST_UNCHECKED -> "[ ] $text"
        FormatType.SUB_HEADING -> "## $text"
        FormatType.CODE -> "```\n$text\n```"
        FormatType.QUOTE -> "> $text"
        // TODO: Fix the fact that markdown parsing wont parse this correctly
        FormatType.IMAGE -> "<image>$text</image>"
        FormatType.SEPARATOR -> "\n---\n"
        FormatType.TEXT -> text

        // NOTE: All the following states should never happen at this place

        FormatType.TAG -> ""
        FormatType.EMPTY -> ""
      }
      markdownBuilder.append(formatMarkdown)
      markdownBuilder.append("\n")
    }
    return markdownBuilder.toString().trim()
  }

  private fun getFormattedDate(): String = formatTimestamp(System.currentTimeMillis(), "dd_MMM_yyyy")
  private fun getFormattedDateWithTime(): String = formatTimestamp(System.currentTimeMillis(), "dd_MMM_yyyy HH_mm")

  private fun getFileForAutomaticExport(): File? {
    val folder = createBackupFolder()
    if (folder === null) {
      return null
    }
    return File(folder, "$AUTO_BACKUP_FILENAME ${getFormattedDate()}.txt")
  }

  private fun createBackupFolder(): File? {
    val folder = File(Environment.getExternalStorageDirectory(), AUTO_BACKUP_FOLDER)
    if (!folder.exists() && !folder.mkdirs()) {
      return null
    }
    return folder
  }
}