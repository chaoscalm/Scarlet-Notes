package com.maubis.scarlet.base.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "widget")
class Widget(
  @PrimaryKey var widgetId: Int,
  var noteUuid: UUID
)