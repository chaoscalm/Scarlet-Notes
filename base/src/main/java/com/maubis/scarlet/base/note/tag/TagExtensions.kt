package com.maubis.scarlet.base.note.tag

import com.maubis.scarlet.base.config.ScarletApp
import com.maubis.scarlet.base.config.ScarletApp.Companion.data
import com.maubis.scarlet.base.database.room.tag.Tag

fun Tag.saveIfUnique() {
  val existing = data.tags.getByTitle(title)
  if (existing !== null) {
    this.uid = existing.uid
    this.uuid = existing.uuid
    return
  }

  val existingByUUID = data.tags.getByUUID(uuid)
  if (existingByUUID != null) {
    this.uid = existingByUUID.uid
    this.title = existingByUUID.title
    return
  }
  save()
}

/**************************************************************************************
 ******************************* Database Functions ********************************
 **************************************************************************************/

fun Tag.save() {
  ScarletApp.data.tagActions(this).save()
}

fun Tag.delete() {
  ScarletApp.data.tagActions(this).delete()
}