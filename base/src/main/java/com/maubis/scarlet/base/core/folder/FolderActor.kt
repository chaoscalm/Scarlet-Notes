package com.maubis.scarlet.base.core.folder

import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.folderSync
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.export.data.ExportableFolder

class FolderActor(val folder: Folder) {
  fun offlineSave() {
    val id = ApplicationBase.instance.foldersRepository.database().insertFolder(folder)
    folder.uid = if (folder.isUnsaved()) id.toInt() else folder.uid

    ApplicationBase.instance.foldersRepository.notifyInsertFolder(folder)
  }

  fun onlineSave() {
    folderSync?.insert(ExportableFolder(folder))
  }

  fun save() {
    offlineSave()
    onlineSave()
  }

  fun offlineDelete() {
    if (folder.isUnsaved()) {
      return
    }
    ApplicationBase.instance.foldersRepository.database().delete(folder)
    ApplicationBase.instance.foldersRepository.notifyDelete(folder)
    folder.uid = 0
  }

  fun delete() {
    offlineDelete()
    folderSync?.remove(ExportableFolder(folder))
  }

}