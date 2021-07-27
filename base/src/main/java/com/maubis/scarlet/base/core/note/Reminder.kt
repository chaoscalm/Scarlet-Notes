package com.maubis.scarlet.base.core.note

import java.util.*

enum class ReminderInterval {
  ONCE,
  DAILY,
}

class LegacyReminder {
  var alarmTimestamp: Long = 0
  var interval: ReminderInterval = ReminderInterval.ONCE
  var daysOfWeek: IntArray = intArrayOf()
}

class Reminder(
  var uid: Int = 0,
  var timestamp: Long = 0,
  var interval: ReminderInterval = ReminderInterval.ONCE) {

  fun toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    return calendar
  }
}
