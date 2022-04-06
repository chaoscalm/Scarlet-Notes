package com.maubis.scarlet.base.backup.data

import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.database.entities.Tag
import java.io.Serializable
import java.util.*

class ExportedTag(
  var uuid: String,
  var title: String
) : Serializable {

  constructor(tag: Tag) : this(tag.uuid.toString(), tag.title)

  fun saveIfNotPresent() {
    val parsedUuid = UUID.fromString(uuid)
    val existingWithSameUuid = ScarletApp.data.tags.getByUUID(parsedUuid)
    if (existingWithSameUuid != null) {
      return
    }

    val tag = Tag(parsedUuid, title)
    tag.save()
  }
}