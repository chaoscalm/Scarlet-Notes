package com.maubis.scarlet.base.support.utils

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.home.sheets.ExceptionBottomSheet
import com.maubis.scarlet.base.support.sheets.openSheet

const val KEY_INTERNAL_SHOW_TRACES_IN_SHEET = "internal_show_traces_in_sheet"
var sInternalShowTracesInSheet: Boolean
  get() = appPreferences.get(KEY_INTERNAL_SHOW_TRACES_IN_SHEET, false)
  set(value) = appPreferences.put(KEY_INTERNAL_SHOW_TRACES_IN_SHEET, value)

fun logAndMaybeDisplayError(activity: AppCompatActivity, thrownException: Exception) {
  if (sInternalShowTracesInSheet) {
    openSheet(activity, ExceptionBottomSheet().apply { this.exception = thrownException })
  }
  logNonCriticalError(thrownException)
}

fun logNonCriticalError(exception: Exception) {
  Log.w("Scarlet", "Non-critical error detected", exception)
}