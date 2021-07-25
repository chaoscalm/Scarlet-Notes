package com.maubis.scarlet.base.backup.data

import com.maubis.scarlet.base.core.folder.IFolderContainer
import com.maubis.scarlet.base.database.entities.Folder
import java.io.Serializable
import java.util.*

class ExportableFolder(
  val uuid: String,
  val title: String,
  val timestamp: Long,
  val updateTimestamp: Long,
  val color: Int
) : Serializable, IFolderContainer {
  override fun timestamp(): Long = timestamp
  override fun updateTimestamp(): Long = updateTimestamp
  override fun color(): Int = color
  override fun title(): String = title
  override fun uuid(): String = uuid

  constructor(folder: Folder) : this(
          folder.uuid,
          folder.title,
          folder.timestamp,
          folder.updateTimestamp,
          folder.color)

  // Default failsafe constructor for Gson to use
  constructor() : this(
          "invalid",
          "",
          Calendar.getInstance().timeInMillis,
          Calendar.getInstance().timeInMillis,
          -0xff8695)
}