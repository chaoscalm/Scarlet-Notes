package com.maubis.scarlet.base.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "widget")
class Widget(
  @PrimaryKey var widgetId: Int,
  var noteUuid: String
)