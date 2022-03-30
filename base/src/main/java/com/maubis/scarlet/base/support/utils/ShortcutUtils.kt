package com.maubis.scarlet.base.support.utils

import android.app.PendingIntent
import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager

fun addShortcut(context: Context, shortcut: ShortcutInfo) {
  if (!OsVersionUtils.canAddLauncherShortcuts()) {
    return
  }

  val shortcutManager = context.getSystemService(ShortcutManager::class.java)
  if (shortcutManager === null) {
    return
  }

  if (shortcutManager.isRequestPinShortcutSupported) {
    val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(shortcut)

    val successCallback = PendingIntent.getBroadcast(
      context, 0,
      pinnedShortcutCallbackIntent, 0)
    shortcutManager.requestPinShortcut(shortcut, successCallback.intentSender)
  }
}