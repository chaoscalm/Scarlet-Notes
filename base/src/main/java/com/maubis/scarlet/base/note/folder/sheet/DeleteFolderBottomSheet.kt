package com.maubis.scarlet.base.note.folder.sheet

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.common.sheets.LithoOptionsItem
import com.maubis.scarlet.base.database.entities.Folder
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState

class DeleteFolderBottomSheet : LithoOptionBottomSheet() {

  var selectedFolder: Folder? = null
  var onDeletionListener: (Folder) -> Unit = { _ -> }

  override fun title(): Int = R.string.folder_delete_option_sheet_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val folder = selectedFolder
    if (folder === null) {
      dismiss()
      return emptyList()
    }

    val activity = context as AppCompatActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
      title = R.string.folder_delete_option_sheet_remove_folder,
      subtitle = R.string.folder_delete_option_sheet_remove_folder_details,
      icon = R.drawable.icon_delete,
      listener = {
        forEachNoteInFolder(folder) {
          it.folder = null
          it.save(activity)
        }
        folder.delete()
        onDeletionListener(folder)
        dismiss()
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.folder_delete_option_sheet_remove_folder_content,
      subtitle = R.string.folder_delete_option_sheet_remove_folder_content_details,
      icon = R.drawable.icon_delete_content,
      listener = {
        forEachNoteInFolder(folder) {
          it.folder = null
          it.updateState(NoteState.TRASH, activity)
        }
        onDeletionListener(folder)
        dismiss()
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.folder_delete_option_sheet_remove_folder_and_content,
      subtitle = R.string.folder_delete_option_sheet_remove_folder_and_content_details,
      icon = R.drawable.ic_delete_permanently,
      listener = {
        forEachNoteInFolder(folder) {
          it.folder = null
          it.updateState(NoteState.TRASH, activity)
        }
        folder.delete()
        onDeletionListener(folder)
        dismiss()
      }
    ))
    return options
  }

  private fun forEachNoteInFolder(folder: Folder, lambda: (Note) -> Unit) {
    data.notes.getAll().filter { it.folder == folder.uuid }.forEach {
      lambda(it)
    }
  }
}