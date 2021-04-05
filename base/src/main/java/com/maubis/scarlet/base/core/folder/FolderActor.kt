package com.maubis.scarlet.base.core.folder

import com.maubis.scarlet.base.config.ScarletApp
import com.maubis.scarlet.base.database.room.folder.Folder

class FolderActor(val folder: Folder) {
  fun save() {
    val id = ScarletApp.data.folders.database().insertFolder(folder)
    folder.uid = if (folder.isUnsaved()) id.toInt() else folder.uid
    ScarletApp.data.folders.notifyInsertFolder(folder)
  }

  fun delete() {
    if (folder.isUnsaved()) {
      return
    }
    ScarletApp.data.folders.database().delete(folder)
    ScarletApp.data.folders.notifyDelete(folder)
    folder.uid = 0
  }
}