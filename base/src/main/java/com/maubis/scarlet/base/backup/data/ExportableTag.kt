package com.maubis.scarlet.base.backup.data

import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.database.entities.Tag
import java.io.Serializable

class ExportableTag(
  var uuid: String,
  var title: String
) : Serializable {

  constructor(tag: Tag) : this(tag.uuid, tag.title)

  fun saveIfNotPresent() {
    val existingWithSameTitle = ScarletApp.data.tags.getByTitle(title)
    if (existingWithSameTitle != null) {
      return
    }
    val existingWithSameUuid = ScarletApp.data.tags.getByUUID(uuid)
    if (existingWithSameUuid != null) {
      return
    }

    val tag = Tag(uuid, title)
    tag.save()
  }
}