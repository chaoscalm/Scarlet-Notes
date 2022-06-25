package com.maubis.scarlet.base.settings

import android.content.SharedPreferences
import androidx.core.content.edit

class AppPreferences(private val sharedPrefs: SharedPreferences) {
  var enableFullscreen: Boolean
    get() = sharedPrefs.getBoolean("internal_enable_full_screen", false)
    set(value) = sharedPrefs.edit { putBoolean("internal_enable_full_screen", value) }

  var showNotesUuids: Boolean
    get() = sharedPrefs.getBoolean("internal_show_uuid", false)
    set(value) = sharedPrefs.edit { putBoolean("internal_show_uuid", value) }

  var noteDefaultColor: Int
    get() = sharedPrefs.getInt("KEY_NOTE_DEFAULT_COLOR", 0xFFD32F2F.toInt())
    set(value) = sharedPrefs.edit { putInt("KEY_NOTE_DEFAULT_COLOR", value) }

  var skipNoteViewer: Boolean
    get() = sharedPrefs.getBoolean("skip_note_viewer", false)
    set(value) = sharedPrefs.edit { putBoolean("skip_note_viewer", value) }

  var editorTextSize: Int
    get() = sharedPrefs.getInt("KEY_TEXT_SIZE", DEFAULT_TEXT_SIZE)
    set(value) = sharedPrefs.edit { putInt("KEY_TEXT_SIZE", value) }

  var liveMarkdownInEditor: Boolean
    get() = sharedPrefs.getBoolean("editor_live_markdown", true)
    set(value) = sharedPrefs.edit { putBoolean("editor_live_markdown", value) }

  var moveCheckedItems: Boolean
    get() = sharedPrefs.getBoolean("editor_move_checked_items", false)
    set(value) = sharedPrefs.edit { putBoolean("editor_move_checked_items", value) }

  companion object {
    const val DEFAULT_TEXT_SIZE = 16
  }
}