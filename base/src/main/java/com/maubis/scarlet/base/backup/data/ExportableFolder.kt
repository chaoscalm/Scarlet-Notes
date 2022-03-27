package com.maubis.scarlet.base.backup.data

import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.database.entities.Folder
import java.io.Serializable

class ExportableFolder(
  val uuid: String,
  val title: String,
  val timestamp: Long,
  val updateTimestamp: Long,
  val color: Int
) : Serializable {

  constructor(folder: Folder) : this(
          folder.uuid,
          folder.title,
          folder.timestamp,
          folder.updateTimestamp,
          folder.color)

  fun saveIfNotPresent() {
    val existingWithSameTitle = ScarletApp.data.folders.getByTitle(title)
    if (existingWithSameTitle != null) {
      return
    }
    val existingWithSameUuid = ScarletApp.data.folders.getByUUID(uuid)
    if (existingWithSameUuid != null) {
      return
    }

    val folder = buildFolder()
    folder.save()
  }

  private fun buildFolder(): Folder {
    val folder = Folder()
    folder.uuid = uuid
    folder.title = title
    folder.timestamp = timestamp
    folder.updateTimestamp = updateTimestamp
    folder.color = color
    return folder
  }
}