package com.maubis.scarlet.base.home

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.NoteState

class NoteDeletionSnackbar(val layout: LinearLayout, val alwaysRunnable: () -> Unit) {

  private val handler = Handler(Looper.getMainLooper())
  private val runnable = {
    layout.visibility = GONE
  }

  val title: TextView = layout.findViewById(R.id.bottom_snackbar_title)
  val action: TextView = layout.findViewById(R.id.bottom_snackbar_action)

  fun softUndo(context: Context, noteBeforeDeletion: Note) {
    if (noteBeforeDeletion.state != NoteState.TRASH) {
      displayMoveToTrashSnackbar(context, noteBeforeDeletion)
      return
    }
    displayPermanentDeletionSnackbar(context, noteBeforeDeletion)
  }

  private fun displayMoveToTrashSnackbar(context: Context, note: Note) {
    val backupOfNote = note.shallowCopy()
    title.setText(R.string.recent_to_trash_message)
    action.setText(R.string.recent_to_trash_undo)
    action.setOnClickListener {
      backupOfNote.save(context)
      alwaysRunnable()
      layout.visibility = GONE
    }
    triggerSnackbar()
  }

  private fun displayPermanentDeletionSnackbar(context: Context, note: Note) {
    val backupOfNote = note.shallowCopy()
    title.setText(R.string.recent_to_delete_message)
    action.setText(R.string.recent_to_trash_undo)
    action.setOnClickListener {
      backupOfNote.save(context)
      alwaysRunnable()
      layout.visibility = GONE
    }
    triggerSnackbar()
  }

  private fun triggerSnackbar() {
    handler.removeCallbacks(runnable)
    layout.visibility = VISIBLE
    handler.postDelayed(runnable, 5 * 1000)
  }
}