package com.maubis.scarlet.base.core.tag

import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.database.room.tag.Tag

class TagActor(val tag: Tag) {
  fun save() {
    val id = ApplicationBase.instance.tagsRepository.database().insertTag(tag)
    tag.uid = if (tag.isUnsaved()) id.toInt() else tag.uid
    ApplicationBase.instance.tagsRepository.notifyInsertTag(tag)
  }

  fun delete() {
    if (tag.isUnsaved()) {
      return
    }
    ApplicationBase.instance.tagsRepository.database().delete(tag)
    ApplicationBase.instance.tagsRepository.notifyDelete(tag)
    tag.uid = 0
  }
}