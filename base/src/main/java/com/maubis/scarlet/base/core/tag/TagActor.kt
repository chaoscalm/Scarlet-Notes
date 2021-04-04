package com.maubis.scarlet.base.core.tag

import com.maubis.scarlet.base.config.ScarletApplication
import com.maubis.scarlet.base.database.room.tag.Tag

class TagActor(val tag: Tag) {
  fun save() {
    val id = ScarletApplication.instance.tagsRepository.database().insertTag(tag)
    tag.uid = if (tag.isUnsaved()) id.toInt() else tag.uid
    ScarletApplication.instance.tagsRepository.notifyInsertTag(tag)
  }

  fun delete() {
    if (tag.isUnsaved()) {
      return
    }
    ScarletApplication.instance.tagsRepository.database().delete(tag)
    ScarletApplication.instance.tagsRepository.notifyDelete(tag)
    tag.uid = 0
  }
}