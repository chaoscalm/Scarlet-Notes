package com.maubis.scarlet.base.note.recycler

import android.content.Context
import android.os.Bundle
import android.view.View
import com.maubis.scarlet.base.common.ui.ThemedActivity
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.editor.ViewNoteActivity
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.note.actions.NoteActionsBottomSheet
import com.maubis.scarlet.base.note.copyToClipboard
import com.maubis.scarlet.base.note.edit
import com.maubis.scarlet.base.note.share
import com.maubis.scarlet.base.security.PincodeBottomSheet

class NoteRecyclerHolder(context: Context, view: View) : NoteRecyclerViewHolderBase(context, view) {

  private val activity = context as MainActivity

  override fun viewClick(note: Note, extra: Bundle?) {
    actionOrUnlockNote(note) { openNote(note) }
  }

  override fun viewLongClick(note: Note, extra: Bundle?) {
    NoteActionsBottomSheet.openSheet(activity, note)
  }

  override fun deleteIconClick(note: Note, extra: Bundle?) {
    actionOrUnlockNote(note) { activity.moveNoteToTrashOrDelete(note) }
  }

  override fun shareIconClick(note: Note, extra: Bundle?) {
    actionOrUnlockNote(note) { note.share(context) }
  }

  override fun editIconClick(note: Note, extra: Bundle?) {
    note.edit(context)
  }

  override fun copyIconClick(note: Note, extra: Bundle?) {
    actionOrUnlockNote(note) { note.copyToClipboard(context) }
  }

  override fun moreOptionsIconClick(note: Note, extra: Bundle?) {
    NoteActionsBottomSheet.openSheet(activity, note)
  }

  private fun actionOrUnlockNote(data: Note, runnable: Runnable) {
    if (context is ThemedActivity && data.locked) {
      PincodeBottomSheet.openForUnlock(context as ThemedActivity,
        onUnlockSuccess = { runnable.run() },
        onUnlockFailure = { actionOrUnlockNote(data, runnable) })
      return
    } else if (data.locked) {
      return
    }
    runnable.run()
  }

  private fun openNote(data: Note) {
    context.startActivity(ViewNoteActivity.makePreferenceAwareIntent(context, data))
  }
}
