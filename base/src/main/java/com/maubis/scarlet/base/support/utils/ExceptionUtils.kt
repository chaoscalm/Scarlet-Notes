package com.maubis.scarlet.base.support.utils

import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.maubis.scarlet.base.config.ApplicationBase.Companion.appPreferences
import com.maubis.scarlet.base.main.sheets.ExceptionBottomSheet
import com.maubis.scarlet.base.support.sheets.openSheet
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val KEY_INTERNAL_SHOW_TRACES_IN_SHEET = "internal_show_traces_in_sheet"
var sInternalShowTracesInSheet: Boolean
  get() = appPreferences.get(KEY_INTERNAL_SHOW_TRACES_IN_SHEET, false)
  set(value) = appPreferences.put(KEY_INTERNAL_SHOW_TRACES_IN_SHEET, value)

const val KEY_INTERNAL_THROW_ON_EXCEPTION = "internal_throw_on_exception"
var sInternalThrowOnException: Boolean
  get() = appPreferences.get(KEY_INTERNAL_THROW_ON_EXCEPTION, false)
  set(value) = appPreferences.put(KEY_INTERNAL_THROW_ON_EXCEPTION, value)

const val KEY_INTERNAL_THROWN_EXCEPTION_COUNT = "internal_thrown_exception_count"
var sInternalThrownExceptionCount: Int
  get() = appPreferences.get(KEY_INTERNAL_THROWN_EXCEPTION_COUNT, 0)
  set(value) = appPreferences.put(KEY_INTERNAL_THROWN_EXCEPTION_COUNT, value)

fun maybeThrow(activity: AppCompatActivity, thrownException: Exception) {
  if (sInternalShowTracesInSheet) {
    openSheet(activity, ExceptionBottomSheet().apply { this.exception = thrownException })
  }
  maybeThrow(thrownException)
}

fun maybeThrow(exception: Exception) {
  if (sInternalThrowOnException) {
    sInternalThrownExceptionCount += 1
    if (sInternalThrownExceptionCount <= 5) {
      GlobalScope.launch {
        SystemClock.sleep(1000)
        throw exception
      }
    }

    sInternalThrownExceptionCount = 0
    sInternalThrowOnException = false
  }

  Log.w("Scarlet", "Non-critical error detected", exception)
}

/**
 * Throws in debug builds and stores the log trace to a fixed note in case of 'internal debug mode'.
 * Else returns the provided value
 */
fun <DataType> throwOrReturn(exception: Exception, result: DataType): DataType {
  maybeThrow(exception)
  return result
}