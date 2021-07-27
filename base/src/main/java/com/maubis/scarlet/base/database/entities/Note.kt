package com.maubis.scarlet.base.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.note.generateUUID
import com.maubis.scarlet.base.settings.sNoteDefaultColor

@Entity(tableName = "note", indices = [Index("uid")])
class Note {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    var description: String = FormatBuilder().getDescription(emptyList())
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
}

enum class NoteState {
    DEFAULT,
    TRASH,
    FAVOURITE,
    ARCHIVED,
}