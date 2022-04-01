package com.maubis.scarlet.base.database.entities

import android.content.Context
import androidx.room.*
import com.google.gson.Gson
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.editor.formats.Format
import com.maubis.scarlet.base.editor.formats.Formats
import com.maubis.scarlet.base.note.mark
import com.maubis.scarlet.base.reminders.Reminder
import com.maubis.scarlet.base.settings.sNoteDefaultColor
import java.util.*

@Entity(tableName = "note", indices = [Index("uuid", unique = true)])
@TypeConverters(NoteConverters::class)
class Note() {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    var uuid: UUID = UUID.randomUUID()
    var content: String = Formats.getNoteContent(emptyList())
    var color: Int = sNoteDefaultColor
    var state: NoteState = NoteState.DEFAULT
    var timestamp: Long = System.currentTimeMillis()
    var updateTimestamp: Long = timestamp
    var locked: Boolean = false
    var pinned: Boolean = false
    var reminder: Reminder? = null
    var excludeFromBackup: Boolean = false
    var tags: String = ""
    var folder: UUID? = null

    constructor(color: Int) : this() {
        this.color = color
    }

    fun isNotPersisted(): Boolean {
        return this.uid == 0
    }

    fun isEqual(note: Note): Boolean {
        return this.content == note.content
                && this.uuid == note.uuid
                && this.tags == note.tags
                && this.timestamp == note.timestamp
                && this.color == note.color
                && this.state == note.state
                && this.locked == note.locked
                && this.pinned == note.pinned
                && this.folder == note.folder
    }

    fun getFormats(): List<Format> {
        return Formats.getFormatsFromNoteContent(this.content)
    }

    fun getTagUUIDs(): MutableSet<UUID> {
        return tags.split(",")
            .filter { it.isNotBlank() }
            .map { UUID.fromString(it) }
            .toMutableSet()
    }

    fun save(context: Context) {
        ScarletApp.data.notes.save(this, context)
    }

    fun delete(context: Context) {
        ScarletApp.data.notes.delete(this, context)
    }

    fun moveToTrashOrDelete(context: Context) {
        if (state === NoteState.TRASH) {
            delete(context)
            return
        }
        mark(context, NoteState.TRASH)
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
}