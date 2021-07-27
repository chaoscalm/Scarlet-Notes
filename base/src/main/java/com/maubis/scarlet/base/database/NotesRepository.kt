package com.maubis.scarlet.base.database

import com.maubis.scarlet.base.core.note.INoteContainer
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.database.daos.NoteDao
import com.maubis.scarlet.base.database.entities.Note
import java.util.concurrent.ConcurrentHashMap

class NotesRepository(val database: NoteDao) {

  val notes = ConcurrentHashMap<String, Note>()

  fun notifyInsertNote(note: Note) {
    maybeLoadFromDB()
    notes[note.uuid] = note
  }

  fun notifyDelete(note: Note) {
    maybeLoadFromDB()
    notes.remove(note.uuid)
  }

  fun getAll(): List<Note> {
    maybeLoadFromDB()
    return notes.values.toList()
  }

  fun getByNoteState(states: Array<NoteState>): List<Note> {
    maybeLoadFromDB()
    return notes.values.filter { states.contains(it.state) }
  }

  fun getNoteByLocked(locked: Boolean): List<Note> {
    maybeLoadFromDB()
    return notes.values.filter { it.locked == locked }
  }

  fun getNoteCountByTag(uuid: String): Int {
    maybeLoadFromDB()
    return notes.values.count { it.tags.contains(uuid) }
  }

  fun getNoteCountByFolder(uuid: String): Int {
    maybeLoadFromDB()
    return notes.values.count { it.folder == uuid }
  }

  fun getByID(uid: Int): Note? {
    maybeLoadFromDB()
    return notes.values.firstOrNull { it.uid == uid }
  }

  fun getByUUID(uuid: String): Note? {
    maybeLoadFromDB()
    return notes[uuid]
  }

  fun getAllUUIDs(): List<String> {
    maybeLoadFromDB()
    return notes.keys.toList()
  }

  fun getLastTimestamp(): Long {
    maybeLoadFromDB()
    return notes.values.map { it.updateTimestamp }.maxOrNull() ?: 0
  }

  fun existingMatch(noteContainer: INoteContainer): Note? {
    maybeLoadFromDB()
    return getByUUID(noteContainer.uuid())
  }

  @Synchronized
  fun maybeLoadFromDB() {
    if (notes.isNotEmpty()) {
      return
    }
    database.getAll().forEach {
      notes[it.uuid] = it
    }
  }
}