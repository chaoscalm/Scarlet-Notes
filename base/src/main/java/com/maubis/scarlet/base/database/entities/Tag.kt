package com.maubis.scarlet.base.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maubis.scarlet.base.ScarletApp
import java.util.*

@Entity(tableName = "tag")
class Tag(@PrimaryKey var uuid: UUID = UUID.randomUUID(), var title: String = "") {
    fun isPersisted(): Boolean = ScarletApp.data.tags.exists(uuid)

    fun save() {
        ScarletApp.data.tags.save(this)
    }

    fun delete() {
        ScarletApp.data.tags.delete(this)
    }
}