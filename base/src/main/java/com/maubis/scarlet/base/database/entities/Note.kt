package com.maubis.scarlet.base.database.entities

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.bijoysingh.starter.util.TextUtils
import com.google.gson.Gson
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.note.NoteMeta
import com.maubis.scarlet.base.core.note.Reminder
import com.maubis.scarlet.base.core.note.generateUUID
import com.maubis.scarlet.base.note.mark
import com.maubis.scarlet.base.settings.sNoteDefaultColor
import com.maubis.scarlet.base.support.utils.logNonCriticalError

@Entity(tableName = "note")
class Note {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    var content: String = FormatBuilder().getContent(emptyList())
    var color: Int = sNoteDefaultColor
    var state: NoteState = NoteState.DEFAULT
    var timestamp: Long = System.currentTimeMillis()
    var updateTimestamp: Long = timestamp
    var locked: Boolean = false
    var pinned: Boolean = false
    var uuid: String = generateUUID()
    var meta: String = ""
    var disableBackup: Boolean = false
    var tags: String = ""
    var folder: String = ""

    fun isNew(): Boolean {
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

    private fun getMeta(): NoteMeta {
        return try {
            Gson().fromJson(this.meta, NoteMeta::class.java) ?: NoteMeta()
        } catch (exception: Exception) {
            logNonCriticalError(exception)
            NoteMeta()
        }
    }

    fun getReminder(): Reminder? {
        return getMeta().reminderV2
    }

    fun setReminder(reminder: Reminder) {
        val noteMeta = NoteMeta()
        noteMeta.reminderV2 = reminder
        meta = Gson().toJson(noteMeta)
    }

    fun getTagUUIDs(): MutableSet<String> {
        return tags.split(",").filter { it.isNotBlank() }.toMutableSet()
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