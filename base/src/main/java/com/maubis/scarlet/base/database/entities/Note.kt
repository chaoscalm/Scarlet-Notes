package com.maubis.scarlet.base.database.entities

import android.content.Context
import androidx.room.*
import com.google.gson.Gson
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.editor.Format
import com.maubis.scarlet.base.editor.FormatType
import com.maubis.scarlet.base.editor.Formats
import com.maubis.scarlet.base.editor.Formats.toNoteContent
import com.maubis.scarlet.base.reminders.Reminder
import com.maubis.scarlet.base.settings.sNoteDefaultColor
import java.util.*

@Entity(tableName = "note", indices = [Index("uuid", unique = true)])
@TypeConverters(NoteConverters::class)
class Note() {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    var uuid: UUID = UUID.randomUUID()
    var content: String = emptyList<Format>().toNoteContent()
    var color: Int = sNoteDefaultColor
    var state: NoteState = NoteState.DEFAULT
    var timestamp: Long = System.currentTimeMillis()
    var updateTimestamp: Long = timestamp
    var locked: Boolean = false
    var pinned: Boolean = false
    var reminder: Reminder? = null
    var excludeFromBackup: Boolean = false
    var tags: MutableSet<UUID> = mutableSetOf<UUID>()
    var folder: UUID? = null

    constructor(color: Int) : this() {
        this.color = color
    }

    fun isNotPersisted(): Boolean {
        return this.uid == 0
    }

    fun isEmpty(): Boolean = contentAsFormats().isEmpty()

    fun contentAsFormats(): List<Format> {
        return Formats.fromNoteContent(this.content)
    }

    fun toggleTag(tag: Tag) {
        if (tags.contains(tag.uuid)) {
            tags.remove(tag.uuid)
        } else {
            tags.add(tag.uuid)
        }
    }

    fun save(context: Context) {
        ScarletApp.data.notes.save(this, context)
    }

    fun delete(context: Context) {
        ScarletApp.data.notes.delete(this, context)
    }

    fun moveToTrashOrDelete(context: Context) {
        if (state == NoteState.TRASH) {
            delete(context)
            return
        }
        updateState(NoteState.TRASH, context)
    }

    fun updateState(state: NoteState, context: Context) {
        this.state = state
        this.updateTimestamp = System.currentTimeMillis()
        save(context)
    }

    fun shallowCopy(): Note {
        val note = Note()
        note.uid = uid
        note.uuid = uuid
        note.state = state
        note.content = content
        note.timestamp = timestamp
        note.updateTimestamp = updateTimestamp
        note.color = color
        note.tags = tags
        note.pinned = pinned
        note.locked = locked
        note.reminder = reminder
        note.folder = folder
        return note
    }

    companion object {
        fun create(title: String, description: String): Note {
            val note = Note()
            val formats = ArrayList<Format>()
            if (title.isNotEmpty()) {
                formats.add(Format(FormatType.HEADING, title))
            }
            formats.add(Format(FormatType.TEXT, description))
            note.content = formats.toNoteContent()
            return note
        }
    }
}

enum class NoteState {
    DEFAULT,
    TRASH,
    FAVOURITE,
    ARCHIVED,
}

object NoteConverters {
    @TypeConverter
    fun reminderToJson(reminder: Reminder?): String? {
        return if (reminder == null) null else Gson().toJson(reminder)
    }

    @TypeConverter
    fun reminderFromJson(reminderJson: String?): Reminder? {
        return try {
            Gson().fromJson(reminderJson, Reminder::class.java)
        } catch (exception: Exception) {
            null
        }
    }

    @TypeConverter
    fun uuidSetToString(uuidSet: MutableSet<UUID>): String = uuidSet.joinToString(separator = ",")

    @TypeConverter
    fun uuidSetFromString(setAsString: String): MutableSet<UUID> {
        return setAsString.split(",")
          .filter { it.isNotBlank() }
          .map { UUID.fromString(it.trim()) }
          .toMutableSet()
    }
}