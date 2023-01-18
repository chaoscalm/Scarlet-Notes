package com.maubis.scarlet.base.common.utils

import android.content.Context
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun readableTime(timestamp: Long, format: String, context: Context): String {
  val adjustedFormat = when {
    DateFormat.is24HourFormat(context) -> format
      .replace("a", "")
      .replace("h", "H")
    else -> format
  }
  return formatTimestamp(timestamp, DateFormat.getBestDateTimePattern(Locale.getDefault(), adjustedFormat))
}

fun formatTimestamp(timestamp: Long, format: String): String {
  val timeFormatter = SimpleDateFormat(format, Locale.getDefault())
  return timeFormatter.format(Date(timestamp))
}
