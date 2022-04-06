package com.maubis.scarlet.base.note.folder.sheet

import android.content.DialogInterface
import android.os.Bundle
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.database.entities.Folder
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.note.actions.INoteActionsActivity

class FolderChooserBottomSheet : FolderChooserBottomSheetBase() {
  private val note: Note by lazy {
    val noteId = requireArguments().getInt(KEY_NOTE_ID)
    data.notes.getByID(noteId) ?: throw IllegalArgumentException("Invalid note ID")
  }

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

  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    (activity as INoteActionsActivity).notifyResetOrDismiss()
  }

  companion object {
    private const val KEY_NOTE_ID = "note_id"

    fun openSheet(activity: ThemedActivity, note: Note) {
      val sheet = FolderChooserBottomSheet()
      sheet.arguments = Bundle().apply { putInt(KEY_NOTE_ID, note.uid) }
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}