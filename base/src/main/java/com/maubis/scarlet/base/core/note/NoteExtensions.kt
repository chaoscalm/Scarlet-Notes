package com.maubis.scarlet.base.core.note

import com.github.bijoysingh.starter.util.TextUtils
import com.google.gson.Gson
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.support.utils.logNonCriticalError

fun Note.isUnsaved(): Boolean {
  return this.uid == 0
}

fun Note.isEqual(note: Note): Boolean {
  return TextUtils.areEqualNullIsEmpty(this.description, note.description)
    && TextUtils.areEqualNullIsEmpty(this.uuid, note.uuid)
    && TextUtils.areEqualNullIsEmpty(this.tags, note.tags)
    && this.timestamp.toLong() == note.timestamp.toLong()
    && this.color.toInt() == note.color.toInt()
    && this.state == note.state
    && this.locked == note.locked
    && this.pinned == note.pinned
    && this.folder == note.folder
}

fun Note.getFormats(): List<Format> {
  return FormatBuilder().getFormats(this.description)
}

fun Note.getMeta(): NoteMeta {
  try {
    return Gson().fromJson(this.meta, NoteMeta::class.java) ?: NoteMeta()
  } catch (exception: Exception) {
    logNonCriticalError(exception)
    return NoteMeta()
  }
}

fun Note.getReminderV2(): Reminder? {
  return getMeta().reminderV2
}

fun Note.setReminderV2(reminder: Reminder) {
  val noteMeta = NoteMeta()
  noteMeta.reminderV2 = reminder
  meta = Gson().toJson(noteMeta)
}

fun Note.getTagUUIDs(): MutableSet<String> {
  val tags = if (this.tags == null) "" else this.tags
  return tags.split(",").filter { it.isNotBlank() }.toMutableSet()
}