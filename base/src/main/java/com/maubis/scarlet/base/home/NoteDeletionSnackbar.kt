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

  val handler = Handler(Looper.getMainLooper())
  val runnable = {
    layout.visibility = GONE
  }

  val title: TextView = layout.findViewById(R.id.bottom_snackbar_title)
  val action: TextView = layout.findViewById(R.id.bottom_snackbar_action)

  fun triggerSnackbar() {
    handler.removeCallbacks(runnable)
    layout.visibility = VISIBLE
    handler.postDelayed(runnable, 5 * 1000)
  }

  fun softUndo(context: Context, note: Note) {
    if (note.state === NoteState.TRASH) {
      undoMoveNoteToTrash(context, note)
      return
    }
    undoDeleteNote(context, note)
  }

  fun undoMoveNoteToTrash(context: Context, note: Note) {
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

  fun undoDeleteNote(context: Context, note: Note) {
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
}