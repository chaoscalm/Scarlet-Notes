package com.maubis.scarlet.base.reminders

import java.util.*

enum class ReminderInterval {
  ONCE,
  DAILY,
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
