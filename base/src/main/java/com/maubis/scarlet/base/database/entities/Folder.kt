package com.maubis.scarlet.base.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maubis.scarlet.base.ScarletApp
import java.util.*

@Entity(tableName = "folder")
class Folder() {
    @PrimaryKey
    var uuid: UUID = UUID.randomUUID()
    var title: String = ""
    var timestamp: Long = System.currentTimeMillis()
    var updateTimestamp: Long = timestamp
    var color: Int = 0

    constructor(color: Int) : this() {
        this.color = color
    }

    fun isNotPersisted(): Boolean = !ScarletApp.data.folders.exists(uuid)

    fun save() {
        ScarletApp.data.folders.save(this)
    }

    fun delete() {
        ScarletApp.data.folders.delete(this)
    }
}