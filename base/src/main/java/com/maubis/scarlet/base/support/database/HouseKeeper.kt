package com.maubis.scarlet.base.support.database

import android.content.Context
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.core.note.NoteImage.Companion.deleteIfExist
import com.maubis.scarlet.base.core.note.ReminderInterval
import com.maubis.scarlet.base.note.reminders.ReminderJob.Companion.nextJobTimestamp
import com.maubis.scarlet.base.note.reminders.ReminderJob.Companion.scheduleJob
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class HouseKeeper(val context: Context) {

  private val houseKeeperTasks: Array<() -> Unit> = arrayOf(
    { removeDecoupledFolders() },
    { removeOldReminders() },
    { deleteRedundantImageFiles() }
  )

  fun execute() {
    for (task in houseKeeperTasks) {
      task()
    }
  }

  private fun removeDecoupledFolders() {
    val folders = data.folders.getAll().map { it.uuid }
    data.notes.getAll()
      .filter { it.folder.isNotBlank() }
      .forEach {
        if (!folders.contains(it.folder)) {
          it.folder = ""
          it.save(context)
        }
      }
  }

  private fun removeOldReminders() {
    data.notes.getAll().forEach {
      val reminder = it.getReminder()
      if (reminder === null) {
        return@forEach
      }

      // Some gap to allow delays in alarm
      if (reminder.timestamp >= System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30)) {
        return@forEach
      }

      if (reminder.interval == ReminderInterval.ONCE) {
        it.meta = ""
        it.save(context)
        return@forEach
      }

      reminder.timestamp = nextJobTimestamp(reminder.timestamp, System.currentTimeMillis())
      reminder.uid = scheduleJob(it.uuid, reminder)
      it.setReminder(reminder)
      it.save(context)
    }
  }

  private fun deleteRedundantImageFiles() {
    val uuids = data.notes.getAllUUIDs()

    val imagesFolder = File(context.filesDir, "images" + File.separator)
    val uuidFiles = imagesFolder.listFiles()
    if (uuidFiles === null || uuidFiles.isEmpty()) {
      return
    }

    val availableDirectories = HashSet<String>()
    for (file in uuidFiles) {
      if (file.isDirectory) {
        availableDirectories.add(file.name)
      }
    }
    for (id in uuids) {
      availableDirectories.remove(id)
    }
    for (uuid in availableDirectories) {
      val noteFolder = File(imagesFolder, uuid)
      for (file in noteFolder.listFiles()) {
        deleteIfExist(file)
      }
    }
  }
}