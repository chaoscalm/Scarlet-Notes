package com.maubis.scarlet.base.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.bijoysingh.starter.util.RandomHelper
import com.maubis.scarlet.base.ScarletApp

@Entity(tableName = "tag", indices = [Index("uid")])
class Tag(var title: String, var uuid: String) {
    @PrimaryKey(autoGenerate = true)
    var uid = 0

    fun isUnsaved(): Boolean {
        return uid == 0
    }

    fun saveIfUnique() {
        val existing = ScarletApp.data.tags.getByTitle(title)
        if (existing !== null) {
            this.uid = existing.uid
            this.uuid = existing.uuid
            return
        }

        val existingByUUID = ScarletApp.data.tags.getByUUID(uuid)
        if (existingByUUID != null) {
            this.uid = existingByUUID.uid
            this.title = existingByUUID.title
            return
        }
        save()
    }

    fun save() {
        ScarletApp.data.tags.save(this)
    }

    fun delete() {
        ScarletApp.data.tags.delete(this)
    }

    companion object {
        fun empty() = Tag("", RandomHelper.getRandomString(24))
    }
}