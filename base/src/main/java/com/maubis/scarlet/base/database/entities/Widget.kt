package com.maubis.scarlet.base.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "widget", indices = [Index("widgetId")])
class Widget(
  @PrimaryKey var widgetId: Int,
  var noteUUID: String
)