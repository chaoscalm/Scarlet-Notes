package com.maubis.scarlet.base.core.folder

import com.maubis.scarlet.base.database.entities.Folder

class FolderBuilder() {
  fun emptyFolder(): Folder {
    return emptyFolder(-0xff8695)
  }

  fun emptyFolder(color: Int): Folder {
    val folder = Folder()
    folder.color = color
    return folder
  }

  fun copy(folderContainer: IFolderContainer): Folder {
    val folder = emptyFolder()
    folder.uuid = folderContainer.uuid()
    folder.title = folderContainer.title()
    folder.timestamp = folderContainer.timestamp()
    folder.updateTimestamp = folderContainer.updateTimestamp()
    folder.color = folderContainer.color()
    return folder
  }
}