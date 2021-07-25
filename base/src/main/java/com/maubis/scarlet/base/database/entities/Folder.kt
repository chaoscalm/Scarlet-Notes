package com.maubis.scarlet.base.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.bijoysingh.starter.util.RandomHelper

@Entity(tableName = "folder", indices = [Index("uid")])
class Folder() {
    @PrimaryKey(autoGenerate = true)
    var uid = 0
    var title: String = ""
    var timestamp: Long = System.currentTimeMillis()
    var updateTimestamp: Long = System.currentTimeMillis()
    var color: Int = 0
    var uuid: String = RandomHelper.getRandomString(24)

    constructor(color: Int) : this() {
        this.color = color
    }
}