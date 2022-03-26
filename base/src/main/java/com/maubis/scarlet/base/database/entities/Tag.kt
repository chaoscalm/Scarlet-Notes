package com.maubis.scarlet.base.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.core.note.generateUUID

@Entity(tableName = "tag")
class Tag(var title: String = "", var uuid: String = generateUUID()) {
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
}