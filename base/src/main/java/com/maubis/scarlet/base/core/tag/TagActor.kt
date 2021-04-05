package com.maubis.scarlet.base.core.tag

import com.maubis.scarlet.base.config.ScarletApp
import com.maubis.scarlet.base.database.room.tag.Tag

class TagActor(val tag: Tag) {
  fun save() {
    val id = ScarletApp.data.tags.database().insertTag(tag)
    tag.uid = if (tag.isUnsaved()) id.toInt() else tag.uid
    ScarletApp.data.tags.notifyInsertTag(tag)
  }

  fun delete() {
    if (tag.isUnsaved()) {
      return
    }
    ScarletApp.data.tags.database().delete(tag)
    ScarletApp.data.tags.notifyDelete(tag)
    tag.uid = 0
  }
}