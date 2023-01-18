package com.maubis.scarlet.base.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.database.entities.NoteState
import com.maubis.scarlet.base.editor.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.note.copyToClipboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationActionReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val noteId = intent.getIntExtra(INTENT_KEY_NOTE_ID, 0)
    if (noteId == 0)
      return

    val receiverResult = goAsync()
    GlobalScope.launch(Dispatchers.IO) {
      try {
        performAction(context, intent, noteId)
      } finally {
        receiverResult.finish()
      }
    }
  }

  private suspend fun performAction(context: Context, intent: Intent, noteId: Int) {
    val note = ScarletApp.data.notes.getByID(noteId) ?: return
    when (intent.action) {
      ACTION_COPY -> withContext(Dispatchers.Main) { note.copyToClipboard(context) }
      ACTION_DELETE -> {
        note.updateState(NoteState.TRASH, context)
        NotificationHandler(context).cancelNotification(note.uid)
      }
    }
  }

  companion object {
    const val ACTION_COPY = "scarlet.NOTE_COPY"
    const val ACTION_DELETE = "scarlet.NOTE_DELETE"
  }
}