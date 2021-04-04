package com.maubis.scarlet.base.core.tag

import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.export.data.ExportableTag

class TagActor(val tag: Tag) {
  fun offlineSave() {
    val id = ApplicationBase.instance.tagsRepository.database().insertTag(tag)
    tag.uid = if (tag.isUnsaved()) id.toInt() else tag.uid
    ApplicationBase.instance.tagsRepository.notifyInsertTag(tag)
  }

  fun onlineSave() {
    ApplicationBase.folderSync?.insert(ExportableTag(tag))
  }

  fun save() {
    offlineSave()
    onlineSave()
  }

  fun offlineDelete() {
    if (tag.isUnsaved()) {
      return
    }
    ApplicationBase.instance.tagsRepository.database().delete(tag)
    ApplicationBase.instance.tagsRepository.notifyDelete(tag)
    tag.uid = 0
  }

  fun delete() {
    offlineDelete()
    ApplicationBase.folderSync?.remove(ExportableTag(tag))
  }
}