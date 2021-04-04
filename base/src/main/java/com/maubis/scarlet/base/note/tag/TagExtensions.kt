package com.maubis.scarlet.base.note.tag

import com.maubis.scarlet.base.config.ScarletApplication
import com.maubis.scarlet.base.config.ScarletApplication.Companion.instance
import com.maubis.scarlet.base.database.room.tag.Tag

fun Tag.saveIfUnique() {
  val existing = instance.tagsRepository.getByTitle(title)
  if (existing !== null) {
    this.uid = existing.uid
    this.uuid = existing.uuid
    return
  }

  val existingByUUID = instance.tagsRepository.getByUUID(uuid)
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
  ScarletApplication.instance.tagActions(this).save()
}

fun Tag.delete() {
  ScarletApplication.instance.tagActions(this).delete()
}