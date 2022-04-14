package com.maubis.scarlet.base.common.utils

import android.app.PendingIntent
import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ShortcutIntentHandlerActivity
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.note.getFullText
import com.maubis.scarlet.base.note.getTitleForSharing

@RequiresApi(Build.VERSION_CODES.O)
object ShortcutHandler {
  fun addLauncherShortcut(context: Context, note: Note) {
    var title = note.getTitleForSharing()
    if (title.isBlank()) {
      title = note.getFullText().split("\n").firstOrNull() ?: "Note"
    }

    val shortcut = ShortcutInfo.Builder(context, shortcutIdFor(note))
      .setShortLabel(title)
      .setLongLabel(title)
      .setIcon(Icon.createWithResource(context, R.mipmap.open_note_launcher))
      .setIntent(ShortcutIntentHandlerActivity.makeOpenNoteShortcutIntent(note))
      .build()
    requestPinnedShortcut(context, shortcut)
  }

  fun disableLauncherShortcuts(context: Context, note: Note, @StringRes disabledNoticeRes: Int) {
    val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return
    shortcutManager.disableShortcuts(listOf(shortcutIdFor(note)), context.getString(disabledNoticeRes))
  }

  private fun shortcutIdFor(note: Note) = "scarlet_notes___${note.uuid}"

  private fun requestPinnedShortcut(context: Context, shortcut: ShortcutInfo) {
    val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return
    if (!shortcutManager.isRequestPinShortcutSupported) {
      Toast.makeText(context, R.string.notice_unsupported_pinned_shortcuts, Toast.LENGTH_LONG).show()
      return
    }

    val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(shortcut)
    val successCallback = PendingIntent.getBroadcast(
      context, 0, pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE)
    shortcutManager.requestPinShortcut(shortcut, successCallback.intentSender)
  }
}