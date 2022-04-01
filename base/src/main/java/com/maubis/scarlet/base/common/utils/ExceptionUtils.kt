package com.maubis.scarlet.base.common.utils

import android.util.Log

fun logNonCriticalError(exception: Exception) {
  Log.w("Scarlet", "Non-critical error detected", exception)
}