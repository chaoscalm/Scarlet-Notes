package com.maubis.scarlet.base.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import com.maubis.scarlet.base.common.ui.Theme
import com.maubis.scarlet.base.common.utils.SortingTechnique

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

  var notesSortingTechnique: SortingTechnique
    get() = SortingTechnique.valueOf(sharedPrefs.getString("notes_sorting_technique", SortingTechnique.LAST_MODIFIED.name)!!)
    set(value) = sharedPrefs.edit { putString("notes_sorting_technique", value.name) }

  var notePreviewLines: Int
    get() = sharedPrefs.getInt("KEY_LINE_COUNT", 7)
    set(value) = sharedPrefs.edit { putInt("KEY_LINE_COUNT", value) }

  var useNoteColorAsBackground: Boolean
    get() = sharedPrefs.getBoolean("KEY_NOTE_VIEWER_BG_COLOR", false)
    set(value) = sharedPrefs.edit { putBoolean("KEY_NOTE_VIEWER_BG_COLOR", value) }

  var displayNotesListAsGrid: Boolean
    get() = sharedPrefs.getBoolean("KEY_LIST_VIEW", true)
    set(isGrid) = sharedPrefs.edit { putBoolean("KEY_LIST_VIEW", isGrid) }

  var selectedTheme: String
    get() = sharedPrefs.getString("KEY_APP_THEME", Theme.DARK.name)!!
    set(value) = sharedPrefs.edit { putString("KEY_APP_THEME", value) }

  var useSystemTheme: Boolean
    get() = sharedPrefs.getBoolean("automatic_theme", false)
    set(value) = sharedPrefs.edit { putBoolean("automatic_theme", value) }

  var darkenCustomColors: Boolean
    get() = sharedPrefs.getBoolean("darken_note_color", false)
    set(value) = sharedPrefs.edit { putBoolean("darken_note_color", value) }

  var showLockedNotesInWidgets: Boolean
    get() = sharedPrefs.getBoolean("widget_show_locked_notes", false)
    set(value) = sharedPrefs.edit { putBoolean("widget_show_locked_notes", value) }

  var showArchivedNotesInWidgets: Boolean
    get() = sharedPrefs.getBoolean("widget_show_archived_notes", true)
    set(value) = sharedPrefs.edit { putBoolean("widget_show_archived_notes", value) }

  var recentNotesWidgetBackground: Int
    get() = sharedPrefs.getInt("widget_background_color_v1", 0x65000000)
    set(value) = sharedPrefs.edit { putInt("widget_background_color_v1", value) }

  var recentNotesWidgetShowToolbar: Boolean
    get() = sharedPrefs.getBoolean("widget_show_toolbar", true)
    set(value) = sharedPrefs.edit { putBoolean("widget_show_toolbar", value) }

  var pinCode: String
    get() = sharedPrefs.getString("KEY_SECURITY_CODE", "")!!
    set(value) = sharedPrefs.edit { putString("KEY_SECURITY_CODE", value) }
  
  var authenticateWithBiometric: Boolean
    get() = sharedPrefs.getBoolean("KEY_FINGERPRINT_ENABLED", true)
    set(value) = sharedPrefs.edit { putBoolean("KEY_FINGERPRINT_ENABLED", value) }
  
  var lockApp: Boolean
    get() = sharedPrefs.getBoolean("app_lock_enabled", false)
    set(value) = sharedPrefs.edit { putBoolean("app_lock_enabled", value) }
  
  var alwaysNeedToAuthenticate: Boolean
    get() = sharedPrefs.getBoolean("ask_pin_always", true)
    set(value) = sharedPrefs.edit { putBoolean("ask_pin_always", value) }

  var backupInMarkdown: Boolean
    get() = sharedPrefs.getBoolean("KEY_BACKUP_MARKDOWN", false)
    set(value) = sharedPrefs.edit { putBoolean("KEY_BACKUP_MARKDOWN", value) }

  var backupLockedNotes: Boolean
    get() = sharedPrefs.getBoolean("KEY_BACKUP_LOCKED", true)
    set(value) = sharedPrefs.edit { putBoolean("KEY_BACKUP_LOCKED", value) }

  var performAutomaticBackups: Boolean
    get() = sharedPrefs.getBoolean("KEY_AUTO_BACKUP_MODE", false)
    set(value) = sharedPrefs.edit { putBoolean("KEY_AUTO_BACKUP_MODE", value) }

  var lastAutomaticBackup: Long
    get() = sharedPrefs.getLong("KEY_AUTO_BACKUP_LAST_TIMESTAMP", 0L)
    set(value) = sharedPrefs.edit { putLong("KEY_AUTO_BACKUP_LAST_TIMESTAMP", value) }
  
  companion object {
    const val DEFAULT_TEXT_SIZE = 16
  }
}