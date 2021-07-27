package com.maubis.scarlet.base.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.core.note.generateUUID
import com.maubis.scarlet.base.support.utils.dateFormat

@Entity(tableName = "folder", indices = [Index("uid")])
class Folder() {
    @PrimaryKey(autoGenerate = true)
    var uid = 0
    var title: String = ""
    var timestamp: Long = System.currentTimeMillis()
    var updateTimestamp: Long = System.currentTimeMillis()
    var color: Int = 0
    var uuid: String = generateUUID()

    constructor(color: Int) : this() {
        this.color = color
    }

    fun isUnsaved(): Boolean {
        return uid == 0
    }

    fun saveIfUnique() {
        val existing = ScarletApp.data.folders.getByTitle(title)
        if (existing !== null) {
            this.uid = existing.uid
            this.uuid = existing.uuid
            return
        }

        val existingByUUID = ScarletApp.data.folders.getByUUID(uuid)
        if (existingByUUID != null) {
            this.uid = existingByUUID.uid
            this.title = existingByUUID.title
            return
        }
        save()
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

    fun save() {
        ScarletApp.data.folders.save(this)
    }

    fun delete() {
        ScarletApp.data.folders.delete(this)
    }
}