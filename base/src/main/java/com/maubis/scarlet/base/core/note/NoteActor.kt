package com.maubis.scarlet.base.core.note

import android.app.NotificationManager
import android.content.Context
import android.os.AsyncTask
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.ScarletApp.Companion.imageStorage
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.mark
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.notification.NotificationConfig
import com.maubis.scarlet.base.notification.NotificationHandler
import com.maubis.scarlet.base.support.utils.OsVersionUtils
import com.maubis.scarlet.base.widget.AllNotesWidgetProvider.Companion.notifyAllChanged
import com.maubis.scarlet.base.widget.WidgetConfigureActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class NoteActor(val note: Note) {
  fun save(context: Context) {
    val id = data.notes.database.insertNote(note)
    note.uid = if (note.isUnsaved()) id.toInt() else note.uid
    data.notes.notifyInsertNote(note)
    GlobalScope.launch {
      onNoteUpdated(context)
    }
  }

  fun softDelete(context: Context) {
    if (note.getNoteState() === NoteState.TRASH) {
      delete(context)
      return
    }
    note.mark(context, NoteState.TRASH)
  }

  fun excludeFromBackups(context: Context) {
    note.disableBackup = true
    note.save(context)
  }

  fun includeInBackups(context: Context) {
    note.disableBackup = false
    note.save(context)
  }

  fun delete(context: Context) {
    imageStorage.deleteAllFiles(note)
    if (note.isUnsaved()) {
      return
    }
    data.notes.database.delete(note)
    data.notes.notifyDelete(note)
    note.description = FormatBuilder().getDescription(ArrayList())
    note.uid = 0
    AsyncTask.execute {
      onNoteDestroyed(context)
    }
  }

  private fun onNoteDestroyed(context: Context) {
    WidgetConfigureActivity.notifyNoteChange(context, note)
    notifyAllChanged(context)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    notificationManager?.cancel(note.uid)
    ScarletApp.imageCache.deleteNote(note.uuid)
  }

  private fun onNoteUpdated(context: Context) {
    WidgetConfigureActivity.notifyNoteChange(context, note)
    notifyAllChanged(context)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    if (OsVersionUtils.canExtractActiveNotifications() && notificationManager != null) {
      for (notification in notificationManager.activeNotifications) {
        if (notification.id == note.uid) {
          val handler = NotificationHandler(context)
          handler.openNotification(NotificationConfig(note = note))
        }
      }
    }
  }

}
