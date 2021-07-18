package com.maubis.scarlet.base.backup.support

import android.os.AsyncTask
import android.os.Environment
import androidx.core.content.edit
import com.github.bijoysingh.starter.util.FileManager
import com.google.gson.Gson
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.backup.data.*
import com.maubis.scarlet.base.backup.sheet.NOTES_EXPORT_FILENAME
import com.maubis.scarlet.base.backup.sheet.NOTES_EXPORT_FOLDER
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.note.getFormats
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.support.utils.dateFormat
import java.io.File

const val KEY_NOTE_VERSION = "KEY_NOTE_VERSION"
const val KEY_AUTO_BACKUP_LAST_TIMESTAMP = "KEY_AUTO_BACKUP_LAST_TIMESTAMP"

const val EXPORT_NOTE_SEPARATOR = ">S>C>A>R>L>E>T>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>N>O>T>E>S>"
const val EXPORT_VERSION = 6

const val AUTO_BACKUP_FILENAME = "auto_backup"
const val AUTO_BACKUP_INTERVAL_MS = 1000 * 60 * 60 * 6 // 6 hours update

const val STORE_KEY_BACKUP_MARKDOWN = "KEY_BACKUP_MARKDOWN"
var sBackupMarkdown: Boolean
  get() = appPreferences.getBoolean(STORE_KEY_BACKUP_MARKDOWN, false)
  set(value) = appPreferences.edit { putBoolean(STORE_KEY_BACKUP_MARKDOWN, value) }

const val STORE_KEY_BACKUP_LOCKED = "KEY_BACKUP_LOCKED"
var sBackupLockedNotes: Boolean
  get() = appPreferences.getBoolean(STORE_KEY_BACKUP_LOCKED, true)
  set(value) = appPreferences.edit { putBoolean(STORE_KEY_BACKUP_LOCKED, value) }

const val STORE_KEY_AUTO_BACKUP_MODE = "KEY_AUTO_BACKUP_MODE"
var sAutoBackupMode: Boolean
  get() = appPreferences.getBoolean(STORE_KEY_AUTO_BACKUP_MODE, false)
  set(value) = appPreferences.edit { putBoolean(STORE_KEY_AUTO_BACKUP_MODE, value) }

class NoteExporter {
  fun getExportContent(): String {
    if (sBackupMarkdown) {
      return getMarkdownExportContent()
    }

    val notes = data.notes
      .getAll()
      .filter { sBackupLockedNotes || !it.locked }
      .map { ExportableNote(it) }
    val tags = data.tags.getAll().map { ExportableTag(it) }
    val folders = data.folders.getAll().map { ExportableFolder(it) }
    val fileContent = ExportableFileFormat(EXPORT_VERSION, notes, tags, folders)
    return Gson().toJson(fileContent)
  }

  private fun getMarkdownExportContent(): String {
    var totalText = "$EXPORT_NOTE_SEPARATOR\n\n"
    data.notes.getAll()
      .map { it.toExportedMarkdown() }
      .forEach {
        totalText += it
        totalText += "\n\n$EXPORT_NOTE_SEPARATOR\n\n"
      }
    return totalText
  }

  /**
   * Converts the note's internal description format into markdown which can be used to export.
   */
  private fun Note.toExportedMarkdown(): String {
    val markdownBuilder = StringBuilder()
    getFormats().forEach { format ->
      val text = format.text
      val formatMarkdown = when (format.formatType) {
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

  fun tryAutoExport() {
    AsyncTask.execute {
      if (!sAutoBackupMode) {
        return@execute
      }
      val lastBackup = appPreferences.getLong(KEY_AUTO_BACKUP_LAST_TIMESTAMP, 0L)
      val lastTimestamp = data.notes.getLastTimestamp()
      if (lastBackup + AUTO_BACKUP_INTERVAL_MS >= lastTimestamp) {
        return@execute
      }

      val exportFile = getOrCreateFileForExport(
        "$AUTO_BACKUP_FILENAME ${dateFormat.getDateForBackup()}")
      if (exportFile === null) {
        return@execute
      }
      saveToFile(exportFile, getExportContent())
      appPreferences.edit { putLong(KEY_AUTO_BACKUP_LAST_TIMESTAMP, System.currentTimeMillis()) }
    }
  }

  fun getOrCreateManualExportFile(): File? {
    return getOrCreateFileForExport(
      "$NOTES_EXPORT_FILENAME ${dateFormat.getTimestampForBackup()}")
  }

  private fun getOrCreateFileForExport(filename: String): File? {
    val folder = createFolder()
    if (folder === null) {
      return null
    }
    return File(folder, filename + ".txt")
  }

  fun saveToManualExportFile(text: String): Boolean {
    val file = getOrCreateManualExportFile()
    if (file === null) {
      return false
    }
    return saveToFile(file, text)
  }

  fun saveToFile(file: File, text: String): Boolean {
    return FileManager.writeToFile(file, text)
  }

  fun createFolder(): File? {
    val folder = File(Environment.getExternalStorageDirectory(), NOTES_EXPORT_FOLDER)
    if (!folder.exists() && !folder.mkdirs()) {
      return null
    }
    return folder
  }
}