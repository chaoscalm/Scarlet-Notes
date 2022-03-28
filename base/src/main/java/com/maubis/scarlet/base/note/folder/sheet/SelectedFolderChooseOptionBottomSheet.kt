package com.maubis.scarlet.base.note.folder.sheet

import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.database.entities.Folder
import com.maubis.scarlet.base.note.selection.SelectNotesActivity
import java.util.*

class SelectedFolderChooseOptionsBottomSheet : FolderChooserBottomSheetBase() {

  var onActionListener: (Folder, Boolean) -> Unit = { _, _ -> }
  var selectedFolders: MutableList<UUID?> = mutableListOf<UUID?>()
  var selectedFolder: UUID? = null

  override fun preComponentRender(componentContext: ComponentContext) {
    val activity = requireContext() as SelectNotesActivity
    selectedFolders.clear()
    selectedFolders.addAll(activity.getAllSelectedNotes().map { it.folder }.distinct())
    selectedFolder = selectedFolders.firstOrNull()
  }

  override fun onFolderSelected(folder: Folder) {
    onActionListener(folder, true)
    onActionListener(folder, folder.uuid != selectedFolder)
  }

  override fun isFolderSelected(folder: Folder): Boolean {
    return folder.uuid == selectedFolder
  }
}