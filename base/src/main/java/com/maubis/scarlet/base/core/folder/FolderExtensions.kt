package com.maubis.scarlet.base.core.folder

import com.maubis.scarlet.base.database.entities.Folder

fun Folder.isUnsaved(): Boolean {
  return uid == 0
}
