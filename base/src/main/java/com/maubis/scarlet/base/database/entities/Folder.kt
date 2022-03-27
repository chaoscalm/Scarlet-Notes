package com.maubis.scarlet.base.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.core.note.generateUUID
import com.maubis.scarlet.base.support.utils.dateFormat

@Entity(tableName = "folder")
class Folder() {
    @PrimaryKey
    var uuid: String = generateUUID()
    var title: String = ""
    var timestamp: Long = System.currentTimeMillis()
    var updateTimestamp: Long = timestamp
    var color: Int = 0

    constructor(color: Int) : this() {
        this.color = color
    }

    fun getDisplayTime(): String {
        val time = when {
            (this.updateTimestamp != 0L) -> this.updateTimestamp
            else -> this.timestamp
        }

        val format = when {
            System.currentTimeMillis() - time < 1000 * 60 * 60 * 2 -> "hh:mm aa"
            else -> "dd MMMM"
        }
        return dateFormat.readableTime(format, time)
    }

    fun isNotPersisted(): Boolean = !ScarletApp.data.folders.exists(uuid)

    fun save() {
        ScarletApp.data.folders.save(this)
    }

    fun delete() {
        ScarletApp.data.folders.delete(this)
    }
}