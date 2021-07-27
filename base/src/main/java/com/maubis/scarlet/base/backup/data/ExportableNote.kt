package com.maubis.scarlet.base.backup.data

import android.content.Context
import com.google.gson.Gson
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.core.note.generateUUID
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.database.entities.Tag
import org.json.JSONArray
import org.json.JSONObject
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
  var meta: Map<String, Any>,
  var folder: String
) : Serializable {
  constructor(note: Note) : this(
    note.uuid,
    note.description,
    note.timestamp,
    note.updateTimestamp,
    note.color,
    note.state.name,
    note.tags,
    emptyMap(),
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
    note.description = description
    note.timestamp = timestamp
    note.updateTimestamp = max(updateTimestamp, timestamp)
    note.color = color
    note.state = runCatching { NoteState.valueOf(state) }.getOrDefault(NoteState.DEFAULT)
    note.tags = tags
    note.meta = Gson().toJson(meta)
    note.folder = folder
    return note
  }

  companion object {

    val KEY_NOTES: String = "notes"

    fun fromJSONObjectV2(json: JSONObject): ExportableNote {
      return ExportableNote(
        generateUUID(),
        json["description"] as String,
        json["timestamp"] as Long,
        json["timestamp"] as Long,
        json["color"] as Int,
        "",
        "",
        emptyMap(),
        "")
    }

    fun fromJSONObjectV3(json: JSONObject): ExportableNote {
      return ExportableNote(
        generateUUID(),
        json["description"] as String,
        json["timestamp"] as Long,
        json["timestamp"] as Long,
        json["color"] as Int,
        json["state"] as String,
        convertTagsJSONArrayToString(json["tags"] as JSONArray),
        emptyMap(),
        "")
    }

    fun fromJSONObjectV4(json: JSONObject): ExportableNote {
      return ExportableNote(
        json["uuid"] as String,
        json["description"] as String,
        json["timestamp"] as Long,
        json["timestamp"] as Long,
        json["color"] as Int,
        json["state"] as String,
        convertTagsJSONArrayToString(json["tags"] as JSONArray),
        emptyMap(),
        "")
    }

    private fun convertTagsJSONArrayToString(tags: JSONArray): String {
      val noteTags = arrayListOf<Tag>()
      for (index in 0 until tags.length()) {
        val tag = ExportableTag.getBestPossibleTagObject(tags.getJSONObject(index))
        tag.save()
        noteTags.add(tag)
      }
      return noteTags.map { it.uuid }.joinToString(separator = ",")
    }
  }
}