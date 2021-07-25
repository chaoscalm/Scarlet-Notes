package com.maubis.scarlet.base.core.tag

import com.github.bijoysingh.starter.util.RandomHelper
import com.maubis.scarlet.base.database.entities.Tag

class TagBuilder() {
  fun emptyTag(): Tag {
    val tag = Tag("", RandomHelper.getRandomString(24))
    tag.uid = 0
    return tag
  }

  fun copy(tagContainer: ITagContainer): Tag {
    return Tag(tagContainer.title(), tagContainer.uuid())
  }
}