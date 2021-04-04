package com.maubis.scarlet.base.core.folder

import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.database.room.folder.Folder

class FolderActor(val folder: Folder) {
  fun save() {
    val id = ApplicationBase.instance.foldersRepository.database().insertFolder(folder)
    folder.uid = if (folder.isUnsaved()) id.toInt() else folder.uid
    ApplicationBase.instance.foldersRepository.notifyInsertFolder(folder)
  }

  fun delete() {
    if (folder.isUnsaved()) {
      return
    }
    ApplicationBase.instance.foldersRepository.database().delete(folder)
    ApplicationBase.instance.foldersRepository.notifyDelete(folder)
    folder.uid = 0
  }
}