package com.maubis.scarlet.base.core.folder

import com.maubis.scarlet.base.config.ScarletApplication
import com.maubis.scarlet.base.database.room.folder.Folder

class FolderActor(val folder: Folder) {
  fun save() {
    val id = ScarletApplication.instance.foldersRepository.database().insertFolder(folder)
    folder.uid = if (folder.isUnsaved()) id.toInt() else folder.uid
    ScarletApplication.instance.foldersRepository.notifyInsertFolder(folder)
  }

  fun delete() {
    if (folder.isUnsaved()) {
      return
    }
    ScarletApplication.instance.foldersRepository.database().delete(folder)
    ScarletApplication.instance.foldersRepository.notifyDelete(folder)
    folder.uid = 0
  }
}