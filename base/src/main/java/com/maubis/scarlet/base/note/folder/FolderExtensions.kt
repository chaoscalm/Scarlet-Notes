package com.maubis.scarlet.base.note.folder

import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.database.entities.Folder
import com.maubis.scarlet.base.support.utils.dateFormat
import java.util.*

fun Folder.saveIfUnique() {
  val existing = data.folders.getByTitle(title)
  if (existing !== null) {
    this.uid = existing.uid
    this.uuid = existing.uuid
    return
  }

  val existingByUUID = data.folders.getByUUID(uuid)
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
    else -> this.timestamp
  }

  val format = when {
    Calendar.getInstance().timeInMillis - time < 1000 * 60 * 60 * 2 -> "hh:mm aa"
    else -> "dd MMMM"
  }
  return dateFormat.readableTime(format, time)
}

fun Folder.save() {
  ScarletApp.data.folders.save(this)
}

fun Folder.delete() {
  ScarletApp.data.folders.delete(this)
}