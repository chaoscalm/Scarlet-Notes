package com.maubis.scarlet.base.backup.data

import android.content.Context
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import java.io.Serializable
import kotlin.math.max

class ExportableNote(
  var uuid: String,
  var description: String,
  var timestamp: Long,
  var updateTimestamp: Long,
  var color: Int,
  var state: String,
  var tags: String,
  var folder: String
) : Serializable {
  constructor(note: Note) : this(
    note.uuid,
    note.content,
    note.timestamp,
    note.updateTimestamp,
    note.color,
    note.state.name,
    note.tags,
    note.folder
  )

  fun saveIfNeeded(context: Context) {
    val existingNote = data.notes.getByUUID(uuid)
    if (existingNote !== null && existingNote.updateTimestamp > this.updateTimestamp) {
      return
    }

    val note = createNote()
    note.save(context)
  }

  private fun createNote(): Note {
    val note = Note()
    note.uuid = uuid
    note.content = description
    note.timestamp = timestamp
    note.updateTimestamp = max(updateTimestamp, timestamp)
    note.color = color
    note.state = runCatching { NoteState.valueOf(state) }.getOrDefault(NoteState.DEFAULT)
    note.tags = tags
    note.folder = folder
    return note
  }
}