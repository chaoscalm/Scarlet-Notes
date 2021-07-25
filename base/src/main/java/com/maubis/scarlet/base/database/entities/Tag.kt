package com.maubis.scarlet.base.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.bijoysingh.starter.util.RandomHelper

@Entity(tableName = "tag", indices = [Index("uid")])
class Tag(var title: String, var uuid: String) {
    @PrimaryKey(autoGenerate = true)
    var uid = 0

    fun isUnsaved(): Boolean {
        return uid == 0
    }

    companion object {
        fun empty() = Tag("", RandomHelper.getRandomString(24))
    }
}