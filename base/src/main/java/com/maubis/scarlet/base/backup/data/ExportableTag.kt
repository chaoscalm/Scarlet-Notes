package com.maubis.scarlet.base.backup.data

import com.maubis.scarlet.base.database.entities.Tag
import java.io.Serializable

class ExportableTag(
  var uuid: String,
  var title: String
) : Serializable {

  // Default failsafe constructor for Gson to use
  constructor() : this("invalid", "")

  constructor(tag: Tag) : this(
          tag.uuid,
          tag.title
  )
}