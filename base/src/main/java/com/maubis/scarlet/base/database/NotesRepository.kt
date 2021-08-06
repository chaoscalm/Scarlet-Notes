package com.maubis.scarlet.base.database

import android.app.NotificationManager
import android.content.Context
import android.os.AsyncTask
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.database.daos.NoteDao
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.notification.NotificationConfig
import com.maubis.scarlet.base.notification.NotificationHandler
import com.maubis.scarlet.base.support.utils.OsVersionUtils
import com.maubis.scarlet.base.widget.AllNotesWidgetProvider
import com.maubis.scarlet.base.widget.WidgetConfigureActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class NotesRepository(private val database: NoteDao) {

  private val notes: ConcurrentHashMap<String, Note> by lazy { loadNotesFromDB() }

  fun getAll(): List<Note> {
    return notes.values.toList()
  }

  fun getByNoteState(states: Array<NoteState>): List<Note> {
    return notes.values.filter { states.contains(it.state) }
  }

  fun getNoteByLocked(locked: Boolean): List<Note> {
    return notes.values.filter { it.locked == locked }
  }

  fun getNoteCountByTag(uuid: String): Int {
    return notes.values.count { it.tags.contains(uuid) }
  }

  fun getNoteCountByFolder(uuid: String): Int {
    return notes.values.count { it.folder == uuid }
  }

  fun getByID(uid: Int): Note? {
    return notes.values.firstOrNull { it.uid == uid }
  }

  fun getByUUID(uuid: String): Note? {
    return notes[uuid]
  }

  fun getAllUUIDs(): List<String> {
    return notes.keys.toList()
  }

  fun getLastTimestamp(): Long {
    return notes.values.map { it.updateTimestamp }.maxOrNull() ?: 0
  }

  fun save(note: Note, context: Context) {
    val id = database.insertNote(note)
    note.uid = if (note.isUnsaved()) id.toInt() else note.uid
    notes[note.uuid] = note
    GlobalScope.launch {
      onNoteUpdated(note, context)
    }
  }

  fun delete(note: Note, context: Context) {
    ScarletApp.imageStorage.deleteAllFiles(note)
    if (note.isUnsaved()) {
      return
    }
    database.delete(note)
    notes.remove(note.uuid)
    note.description = FormatBuilder().getDescription(ArrayList())
    note.uid = 0
    AsyncTask.execute {
      onNoteDestroyed(note, context)
    }
  }

  private fun onNoteUpdated(note: Note, context: Context) {
    WidgetConfigureActivity.notifyNoteChange(context, note)
    AllNotesWidgetProvider.notifyAllChanged(context)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    if (OsVersionUtils.canExtractActiveNotifications() && notificationManager != null) {
      for (notification in notificationManager.activeNotifications) {
        if (notification.id == note.uid) {
          val handler = NotificationHandler(context)
          handler.openNotification(NotificationConfig(note = note))
        }
      }
    }
  }

  private fun onNoteDestroyed(note: Note, context: Context) {
    WidgetConfigureActivity.notifyNoteChange(context, note)
    AllNotesWidgetProvider.notifyAllChanged(context)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    notificationManager?.cancel(note.uid)
    ScarletApp.imageCache.deleteNote(note.uuid)
  }

  private fun loadNotesFromDB(): ConcurrentHashMap<String, Note> {
    val notesMap = ConcurrentHashMap<String, Note>()
    database.getAll().forEach { notesMap[it.uuid] = it }
    return notesMap
  }
}