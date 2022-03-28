package com.maubis.scarlet.base.database.entities

import android.content.Context
import androidx.room.*
import com.github.bijoysingh.starter.util.TextUtils
import com.google.gson.Gson
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.note.Reminder
import com.maubis.scarlet.base.core.note.generateUUID
import com.maubis.scarlet.base.note.mark
import com.maubis.scarlet.base.settings.sNoteDefaultColor
import java.util.*

@Entity(tableName = "note", indices = [Index("uuid", unique = true)])
@TypeConverters(NoteConverters::class)
class Note {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    var uuid: String = generateUUID()
    var content: String = FormatBuilder().getContent(emptyList())
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

    fun isNotPersisted(): Boolean {
        return this.uid == 0
    }

    fun isEqual(note: Note): Boolean {
        return TextUtils.areEqualNullIsEmpty(this.content, note.content)
                && TextUtils.areEqualNullIsEmpty(this.uuid, note.uuid)
                && TextUtils.areEqualNullIsEmpty(this.tags, note.tags)
                && this.timestamp == note.timestamp
                && this.color == note.color
                && this.state == note.state
                && this.locked == note.locked
                && this.pinned == note.pinned
                && this.folder == note.folder
    }

    fun getFormats(): List<Format> {
        return FormatBuilder().getFormats(this.content)
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

    fun softDelete(context: Context) {
        if (state === NoteState.TRASH) {
            delete(context)
            return
        }
        mark(context, NoteState.TRASH)
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