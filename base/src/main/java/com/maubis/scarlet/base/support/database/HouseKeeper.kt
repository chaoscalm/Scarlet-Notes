package com.maubis.scarlet.base.support.database

import android.content.Context
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.core.note.ReminderInterval
import com.maubis.scarlet.base.note.reminders.ReminderJob.Companion.nextJobTimestamp
import com.maubis.scarlet.base.note.reminders.ReminderJob.Companion.scheduleJob
import java.util.concurrent.TimeUnit

class HouseKeeper(val context: Context) {

  private val houseKeeperTasks: Array<() -> Unit> = arrayOf(
    { removeDecoupledFolders() },
    { removeOldReminders() }
  )

  fun execute() {
    for (task in houseKeeperTasks) {
      task()
    }
  }

  private fun removeDecoupledFolders() {
    val folders = data.folders.getAll().map { it.uuid }
    data.notes.getAll()
      .filter { it.folder != null }
      .forEach {
        if (!folders.contains(it.folder)) {
          it.folder = null
          it.save(context)
        }
      }
  }

  private fun removeOldReminders() {
    data.notes.getAll().forEach {
      val reminder = it.reminder
      if (reminder === null) {
        return@forEach
      }

      // Some gap to allow delays in alarm
      if (reminder.timestamp >= System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30)) {
        return@forEach
      }

      if (reminder.interval == ReminderInterval.ONCE) {
        it.reminder = null
        it.save(context)
        return@forEach
      }

      reminder.timestamp = nextJobTimestamp(reminder.timestamp, System.currentTimeMillis())
      reminder.uid = scheduleJob(it.uuid, reminder)
      it.reminder = reminder
      it.save(context)
    }
  }
}