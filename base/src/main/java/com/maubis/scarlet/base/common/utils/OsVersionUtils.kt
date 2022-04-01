package com.maubis.scarlet.base.common.utils

import android.os.Build

object OsVersionUtils {
  fun canSetStatusBarTheme() = Build.VERSION.SDK_INT >= 23

  fun canExtractActiveNotifications() = Build.VERSION.SDK_INT >= 23

  fun canAddLauncherShortcuts() = Build.VERSION.SDK_INT >= 26

  fun canAddNotificationChannels() = Build.VERSION.SDK_INT >= 26

  fun canUseSystemTheme() = Build.VERSION.SDK_INT >= 29
}
