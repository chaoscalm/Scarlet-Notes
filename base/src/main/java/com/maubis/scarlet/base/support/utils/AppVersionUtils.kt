package com.maubis.scarlet.base.support.utils

import com.maubis.scarlet.base.BuildConfig
import com.maubis.scarlet.base.config.ScarletApplication.Companion.appPreferences
import com.maubis.scarlet.base.config.ScarletApplication.Companion.instance
import com.maubis.scarlet.base.main.sheets.WHATS_NEW_SHEET_INDEX

const val KEY_LAST_KNOWN_APP_VERSION = "KEY_LAST_KNOWN_APP_VERSION"
const val KEY_LAST_SHOWN_WHATS_NEW = "KEY_LAST_SHOWN_WHATS_NEW"

fun getCurrentVersionCode(): Int {
  return BuildConfig.VERSION_CODE
}

/**
 * Returns app version if it's guaranteed the user an app version. (Stored in the app version variable)
 * If the user has notes it is assumed that the user was at-least at the last version. returns : -1
 * If nothing can be concluded it's 0 (assumes new user)
 */
fun getLastUsedAppVersionCode(): Int {
  val appVersion = appPreferences.get(KEY_LAST_KNOWN_APP_VERSION, 0)
  return when {
    appVersion > 0 -> appVersion
    instance.notesRepository.getCount() > 0 -> -1
    else -> 0
  }
}

fun shouldShowWhatsNewSheet(): Boolean {
  val lastShownWhatsNew = appPreferences.get(KEY_LAST_SHOWN_WHATS_NEW, 0)
  if (lastShownWhatsNew >= WHATS_NEW_SHEET_INDEX) {
    // Already shown the latest
    return false
  }

  val lastUsedAppVersion = getLastUsedAppVersionCode()

  // Update the values independent of the decision
  appPreferences.put(KEY_LAST_SHOWN_WHATS_NEW, WHATS_NEW_SHEET_INDEX)
  appPreferences.put(KEY_LAST_KNOWN_APP_VERSION, getCurrentVersionCode())

  // New users don't need to see the whats new screen
  return lastUsedAppVersion != 0
}
