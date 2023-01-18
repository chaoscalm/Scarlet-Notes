package com.maubis.scarlet.base.database

import android.content.Context
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.common.utils.OsVersionUtils
import com.maubis.scarlet.base.common.utils.ShortcutHandler
import com.maubis.scarlet.base.database.daos.NoteDao
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.notification.NotificationConfig
import com.maubis.scarlet.base.notification.NotificationHandler
import com.maubis.scarlet.base.widget.RecentNotesWidgetProvider
import com.maubis.scarlet.base.widget.WidgetConfigureActivity
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class NotesRepository(private val database: NoteDao, private val notificationHandler: NotificationHandler) {

  private val notes: ConcurrentHashMap<UUID, Note> by lazy { loadNotesFromDB() }

  fun getAll(): List<Note> {
    return notes.values.toList()
  }

  fun getByNoteState(vararg states: NoteState): List<Note> {
    return notes.values.filter { states.contains(it.state) }
  }

  fun getNoteByLocked(locked: Boolean): List<Note> {
    return notes.values.filter { it.locked == locked }
  }

  fun getNoteCountByFolder(folderUuid: UUID): Int {
    return notes.values.count { it.folder == folderUuid }
  }

  fun getByID(uid: Int): Note? {
    return notes.values.firstOrNull { it.uid == uid }
  }

  fun getByUUID(uuid: UUID): Note? {
    return notes[uuid]
  }

  fun getLastTimestamp(): Long {
    return notes.values.map { it.updateTimestamp }.maxOrNull() ?: 0
  }

  fun save(note: Note, context: Context) {
    val isUpdatingExistingNote = !note.isNotPersisted()
    val id = database.insertNote(note)
    note.uid = id.toInt()
    notes[note.uuid] = note
    if (isUpdatingExistingNote) {
      onNoteUpdated(note, context)
    }
  }

  fun delete(note: Note, context: Context) {
    if (note.isNotPersisted()) {
      ScarletApp.imageStorage.deleteAllFiles(note)
      return
    }
    database.delete(note)
    notes.remove(note.uuid)
    note.uid = 0
    onNoteDestroyed(note, context)
  }

  private fun onNoteUpdated(note: Note, context: Context) {
    WidgetConfigureActivity.notifyNoteChange(context, note)
    RecentNotesWidgetProvider.notifyAllChanged(context)
    notificationHandler.updateExistingNotification(NotificationConfig(note))
  }

  private fun onNoteDestroyed(note: Note, context: Context) {
    WidgetConfigureActivity.notifyNoteChange(context, note)
    RecentNotesWidgetProvider.notifyAllChanged(context)
    notificationHandler.cancelNotification(note.uid)
    if (OsVersionUtils.canAddLauncherShortcuts())
      ShortcutHandler.disableLauncherShortcuts(context, note, R.string.recent_to_delete_message)
    ScarletApp.imageStorage.deleteAllFiles(note)
  }

  private fun loadNotesFromDB(): ConcurrentHashMap<UUID, Note> {
    val notesMap = ConcurrentHashMap<UUID, Note>()
    database.getAll().forEach { notesMap[it.uuid] = it }
    return notesMap
  }
}