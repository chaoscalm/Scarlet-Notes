package com.maubis.scarlet.base.note.folder

import com.maubis.scarlet.base.config.ScarletApplication
import com.maubis.scarlet.base.config.ScarletApplication.Companion.instance
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.support.utils.dateFormat
import java.util.*

fun Folder.saveIfUnique() {
  val existing = instance.foldersRepository.getByTitle(title)
  if (existing !== null) {
    this.uid = existing.uid
    this.uuid = existing.uuid
    return
  }

  val existingByUUID = instance.foldersRepository.getByUUID(uuid)
  if (existingByUUID != null) {
    this.uid = existingByUUID.uid
    this.title = existingByUUID.title
    return
  }
  save()
}

fun Folder.getDisplayTime(): String {
  val time = when {
    (this.updateTimestamp != 0L) -> this.updateTimestamp
    (this.timestamp != null) -> this.timestamp
    else -> 0
  }

  val format = when {
    Calendar.getInstance().timeInMillis - time < 1000 * 60 * 60 * 2 -> "hh:mm aa"
    else -> "dd MMMM"
  }
  return dateFormat.readableTime(format, time)
}

/**************************************************************************************
 ******************************* Database Functions ********************************
 **************************************************************************************/

fun Folder.save() {
  ScarletApplication.instance.folderActions(this).save()
}

fun Folder.delete() {
  ScarletApplication.instance.folderActions(this).delete()
}