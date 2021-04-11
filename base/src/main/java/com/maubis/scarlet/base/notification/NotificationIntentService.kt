package com.maubis.scarlet.base.notification

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.note.copy
import com.maubis.scarlet.base.note.creation.activity.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.note.share
import com.maubis.scarlet.base.support.utils.logNonCriticalError

const val INTENT_KEY_ACTION = "ACTION"

class NotificationIntentService : IntentService("NotificationIntentService") {

  override fun onHandleIntent(intent: Intent?) {
    if (intent === null) {
      return
    }

    val context = applicationContext
    if (context === null) {
      return
    }

    val action = getAction(intent.getStringExtra(INTENT_KEY_ACTION))
    if (action === null) {
      return
    }

    val noteId = intent.getIntExtra(INTENT_KEY_NOTE_ID, 0)
    if (noteId == 0) {
      return
    }

    val note = data.notes.getByID(noteId)
    if (note === null) {
      return
    }

    when (action) {
      NoteAction.COPY -> note.copy(context)
      NoteAction.SHARE -> note.share(context)
      NoteAction.DELETE -> {
        ScarletApp.data.noteActions(note).softDelete(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(note.uid)
      }
    }
  }

  private fun getAction(action: String?): NoteAction? {
    if (action === null) {
      return null
    }

    try {
      return NoteAction.valueOf(action)
    } catch (exception: Exception) {
      logNonCriticalError(exception)
      return null
    }
  }

  enum class NoteAction {
    COPY,
    SHARE,
    DELETE,
  }
}