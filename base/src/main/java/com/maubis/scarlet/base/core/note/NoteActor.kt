package com.maubis.scarlet.base.core.note

import android.app.NotificationManager
import android.content.Context
import android.os.AsyncTask
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.appImageStorage
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.main.activity.WidgetConfigureActivity
import com.maubis.scarlet.base.note.getFullText
import com.maubis.scarlet.base.note.getTitleForSharing
import com.maubis.scarlet.base.note.mark
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.notification.NotificationConfig
import com.maubis.scarlet.base.notification.NotificationHandler
import com.maubis.scarlet.base.support.utils.OsVersionUtils
import com.maubis.scarlet.base.widget.AllNotesWidgetProvider.Companion.notifyAllChanged
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class NoteActor(val note: Note) {
  fun copy(context: Context) {
    TextUtils.copyToClipboard(context, note.getFullText())
  }

  fun share(context: Context) {
    IntentUtils.ShareBuilder(context)
      .setSubject(note.getTitleForSharing())
      .setText(note.getFullText())
      .setChooserText(context.getString(R.string.share_using))
      .share()
  }

  fun save(context: Context) {
    val id = instance.notesRepository.database().insertNote(note)
    note.uid = if (note.isUnsaved()) id.toInt() else note.uid
    instance.notesRepository.notifyInsertNote(note)
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
    appImageStorage.deleteAllFiles(note)
    if (note.isUnsaved()) {
      return
    }
    instance.notesRepository.database().delete(note)
    instance.notesRepository.notifyDelete(note)
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
    ApplicationBase.appImageCache.deleteNote(note.uuid)
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
