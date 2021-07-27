package com.maubis.scarlet.base.note.reminders

import android.util.Log
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.core.note.Reminder
import com.maubis.scarlet.base.core.note.ReminderInterval
import com.maubis.scarlet.base.notification.NotificationConfig
import com.maubis.scarlet.base.notification.NotificationHandler
import com.maubis.scarlet.base.notification.REMINDER_NOTIFICATION_CHANNEL_ID
import java.util.concurrent.TimeUnit

class ReminderJob : Job() {

  override fun onRunJob(params: Params): Result {
    val noteUUID = params.extras.getString(EXTRA_KEY_NOTE_UUID, "")
    val note = data.notes.getByUUID(noteUUID)
    if (note === null) {
      return Result.SUCCESS
    }

    val handler = NotificationHandler(context)
    handler.openNotification(NotificationConfig(note, REMINDER_NOTIFICATION_CHANNEL_ID))

    try {
      val reminder = note.getReminderV2()
      if (reminder?.interval == ReminderInterval.DAILY) {
        val reminderV2 = Reminder(
          0,
          nextJobTimestamp(reminder.timestamp, System.currentTimeMillis()),
          ReminderInterval.DAILY)
        reminderV2.uid = scheduleJob(note.uuid, reminderV2)
        note.setReminderV2(reminderV2)
        note.save(context)
      } else {
        note.meta = ""
        note.save(context)
      }
    } catch (exception: Exception) {
      Log.e("Scarlet", "Error while updating note reminder", exception)
    }

    return Result.SUCCESS
  }

  companion object {
    val TAG = "reminder_job"
    val EXTRA_KEY_NOTE_UUID = "note_uuid"

    fun scheduleJob(noteUuid: String, reminder: Reminder): Int {
      val extras = PersistableBundleCompat()
      extras.putString(EXTRA_KEY_NOTE_UUID, noteUuid)

      var deltaTime = reminder.timestamp - System.currentTimeMillis()
      if (reminder.interval == ReminderInterval.DAILY && deltaTime > TimeUnit.DAYS.toMillis(1)) {
        deltaTime = deltaTime % TimeUnit.DAYS.toMillis(1)
      }

      return JobRequest.Builder(ReminderJob.TAG)
        .setExact(deltaTime)
        .setExtras(extras)
        .build()
        .schedule()
    }

    fun nextJobTimestamp(timestamp: Long, currentTimestamp: Long): Long {
      return when {
        timestamp > currentTimestamp -> timestamp
        else -> {
          var tempTimestamp = timestamp
          while (tempTimestamp <= currentTimestamp) {
            tempTimestamp += TimeUnit.DAYS.toMillis(1)
          }
          tempTimestamp
        }
      }
    }

    fun cancelJob(uid: Int) {
      JobManager.instance().cancel(uid);
    }
  }
}