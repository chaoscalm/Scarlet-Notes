package com.maubis.scarlet.base.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tag", indices = [Index("uid")])
class Tag(var title: String, var uuid: String) {
    @PrimaryKey(autoGenerate = true)
    var uid = 0
}