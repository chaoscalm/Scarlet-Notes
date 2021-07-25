package com.maubis.scarlet.base.core.tag

import com.maubis.scarlet.base.database.entities.Tag

fun Tag.isUnsaved(): Boolean {
  return uid == 0
}
