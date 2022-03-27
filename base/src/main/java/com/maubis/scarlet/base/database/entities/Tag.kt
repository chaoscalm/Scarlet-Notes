package com.maubis.scarlet.base.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.core.note.generateUUID

@Entity(tableName = "tag")
class Tag(@PrimaryKey var uuid: String = generateUUID(), var title: String = "") {
    fun isNotPersisted(): Boolean = !ScarletApp.data.tags.exists(uuid)

    fun save() {
        ScarletApp.data.tags.save(this)
    }

    fun delete() {
        ScarletApp.data.tags.delete(this)
    }
}