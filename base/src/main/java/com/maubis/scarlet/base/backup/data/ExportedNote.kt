package com.maubis.scarlet.base.backup.data

import android.content.Context
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import java.io.Serializable
import java.util.*
import kotlin.math.max

class ExportedNote(
  var uuid: String,
  var description: String,
  var timestamp: Long,
  var updateTimestamp: Long,
  var color: Int,
  var state: String,
  var tags: String,
  var folder: String,
  var pinned: Boolean,
  var locked: Boolean
) : Serializable {
  constructor(note: Note) : this(
    note.uuid,
    note.content,
    note.timestamp,
    note.updateTimestamp,
    note.color,
    note.state.name,
    note.tags,
    note.folder?.toString() ?: "",
    note.pinned,
    note.locked
  )

  fun saveIfNeeded(context: Context) {
    val existingNote = data.notes.getByUUID(uuid)
    if (existingNote != null && existingNote.updateTimestamp > this.updateTimestamp) {
      return
    }

    val note = buildNote()
    note.save(context)
  }

  private fun buildNote(): Note {
    val note = Note()
    note.uuid = uuid
    note.content = description
    note.timestamp = timestamp
    note.updateTimestamp = max(updateTimestamp, timestamp)
    note.color = color
    note.state = runCatching { NoteState.valueOf(state) }.getOrDefault(NoteState.DEFAULT)
    note.tags = tags
    note.folder = if (folder.isBlank()) null else UUID.fromString(folder)
    note.pinned = pinned
    note.locked = locked
    return note
  }
}