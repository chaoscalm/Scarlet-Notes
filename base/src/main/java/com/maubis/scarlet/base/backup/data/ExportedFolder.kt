package com.maubis.scarlet.base.backup.data

import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.database.entities.Folder
import java.io.Serializable
import java.util.*

class ExportedFolder(
  val uuid: String,
  val title: String,
  val timestamp: Long,
  val updateTimestamp: Long,
  val color: Int
) : Serializable {

  constructor(folder: Folder) : this(
          folder.uuid.toString(),
          folder.title,
          folder.timestamp,
          folder.updateTimestamp,
          folder.color)

  fun saveIfNeeded() {
    val existingFolder = ScarletApp.data.folders.getByUUID(UUID.fromString(uuid))
    if (existingFolder != null && existingFolder.updateTimestamp > this.updateTimestamp) {
      return
    }

    val folder = buildFolder()
    folder.save()
  }

  private fun buildFolder(): Folder {
    val folder = Folder()
    folder.uuid = UUID.fromString(uuid)
    folder.title = title
    folder.timestamp = timestamp
    folder.updateTimestamp = updateTimestamp
    folder.color = color
    return folder
  }
}