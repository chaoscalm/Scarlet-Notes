package com.maubis.scarlet.base.note.folder.sheet

import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.database.entities.Folder
import com.maubis.scarlet.base.database.entities.Note

class FolderChooserBottomSheet(private val note: Note) : FolderChooserBottomSheetBase() {

  override fun preComponentRender(componentContext: ComponentContext) {}

  override fun onFolderSelected(folder: Folder) {
    note.folder = when (note.folder) {
      folder.uuid -> null
      else -> folder.uuid
    }
    note.save(requireContext())
  }

  override fun isFolderSelected(folder: Folder): Boolean {
    return note.folder == folder.uuid
  }
}