package com.maubis.scarlet.base.backup

import android.os.Environment
import com.google.gson.Gson
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.backup.data.ExportFileFormat
import com.maubis.scarlet.base.backup.data.ExportedFolder
import com.maubis.scarlet.base.backup.data.ExportedNote
import com.maubis.scarlet.base.backup.data.ExportedTag
import com.maubis.scarlet.base.common.utils.formatTimestamp
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.editor.FormatType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream

const val EXPORT_NOTE_SEPARATOR = "----------------------------------------"
const val EXPORT_VERSION = 6

const val AUTO_BACKUP_FOLDER = "ScarletNotes"
const val AUTO_BACKUP_INTERVAL_MS = 1000 * 60 * 60 * 6 // 6 hours update

object NoteExporter {
  fun tryAutoExport() {
    GlobalScope.launch(Dispatchers.IO) {
      if (!ScarletApp.prefs.performAutomaticBackups) {
        return@launch
      }
      val lastTimestamp = data.notes.getLastTimestamp()
      if (ScarletApp.prefs.lastAutomaticBackup + AUTO_BACKUP_INTERVAL_MS >= lastTimestamp) {
        return@launch
      }

      val exportFile = getFileForAutomaticExport()
      if (exportFile === null) {
        return@launch
      }
      exportFile.writeText(getBackupFileContent())
      ScarletApp.prefs.lastAutomaticBackup = System.currentTimeMillis()
    }
  }

  fun getDefaultManualBackupFileName(): String {
    return "Scarlet_backup_${getFormattedDateWithTime()}.txt"
  }

  fun getDefaultMarkdownExportFileName(): String {
    return "Scarlet_notes_export_${getFormattedDateWithTime()}.md"
  }

  fun exportNotesToFile(file: FileDescriptor) {
    val backupContent = getBackupFileContent()
    FileOutputStream(file).use { it.write(backupContent.toByteArray()) }
  }

  private fun getBackupFileContent(): String {
    val notesToBeExported = data.notes.getAll()
      .filter { ScarletApp.prefs.backupLockedNotes || !it.locked }
      .filter { !it.excludeFromBackup }

    if (ScarletApp.prefs.backupInMarkdown) {
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
    contentAsFormats().forEach { format ->
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
        FormatType.TAG -> ""
      }
      markdownBuilder.append(formatMarkdown)
      markdownBuilder.append("\n")
    }
    return markdownBuilder.toString().trim()
  }

  private fun getFormattedDate(): String = formatTimestamp(System.currentTimeMillis(), "yyyy-MM-dd")
  private fun getFormattedDateWithTime(): String = formatTimestamp(System.currentTimeMillis(), "yyyy-MM-dd_HH.mm")

  private fun getFileForAutomaticExport(): File? {
    val folder = createBackupFolder()
    if (folder === null) {
      return null
    }
    return File(folder, "auto_backup_${getFormattedDate()}.txt")
  }

  private fun createBackupFolder(): File? {
    val folder = File(Environment.getExternalStorageDirectory(), AUTO_BACKUP_FOLDER)
    if (!folder.exists() && !folder.mkdirs()) {
      return null
    }
    return folder
  }
}