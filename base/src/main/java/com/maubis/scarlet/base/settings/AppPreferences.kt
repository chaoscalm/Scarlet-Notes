package com.maubis.scarlet.base.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import com.maubis.scarlet.base.common.ui.Theme
import com.maubis.scarlet.base.common.utils.SortingTechnique
import kotlin.reflect.KProperty

class AppPreferences(private val sharedPrefs: SharedPreferences) {
  var enableFullscreen: Boolean by BooleanPreference("internal_enable_full_screen", false)
  var showNotesUuids: Boolean by BooleanPreference("internal_show_uuid", false)

  var noteDefaultColor: Int by IntPreference("KEY_NOTE_DEFAULT_COLOR", 0xFFD32F2F.toInt())
  var useNoteColorAsBackground: Boolean by BooleanPreference("KEY_NOTE_VIEWER_BG_COLOR", false)
  var skipNoteViewer: Boolean by BooleanPreference("skip_note_viewer", false)
  var editorTextSize: Int by IntPreference("KEY_TEXT_SIZE", DEFAULT_TEXT_SIZE)
  var liveMarkdownInEditor: Boolean by BooleanPreference("editor_live_markdown", true)
  var moveCheckedItems: Boolean by BooleanPreference("editor_move_checked_items", false)

  var notesSortingTechnique: SortingTechnique
    get() = SortingTechnique.valueOf(sharedPrefs.getString("notes_sorting_technique", SortingTechnique.LAST_MODIFIED.name)!!)
    set(value) = sharedPrefs.edit { putString("notes_sorting_technique", value.name) }

  var notePreviewLines: Int by IntPreference("KEY_LINE_COUNT", 7)
  var displayNotesListAsGrid: Boolean by BooleanPreference("KEY_LIST_VIEW", true)

  var selectedTheme: String by StringPreference("KEY_APP_THEME", Theme.DARK.name)
  var useSystemTheme: Boolean by BooleanPreference("automatic_theme", false)
  var darkenCustomColors: Boolean by BooleanPreference("darken_note_color", false)

  var showLockedNotesInWidgets: Boolean by BooleanPreference("widget_show_locked_notes", false)
  var showArchivedNotesInWidgets: Boolean by BooleanPreference("widget_show_archived_notes", true)
  var recentNotesWidgetBackground: Int by IntPreference("widget_background_color_v1", 0x65000000)
  var recentNotesWidgetShowToolbar: Boolean by BooleanPreference("widget_show_toolbar", true)

  var pinCode: String by StringPreference("KEY_SECURITY_CODE", "")
  var lockApp: Boolean by BooleanPreference("app_lock_enabled", false)
  var authenticateWithBiometric: Boolean by BooleanPreference("KEY_FINGERPRINT_ENABLED", true)
  var alwaysNeedToAuthenticate: Boolean by BooleanPreference("ask_pin_always", true)

  var backupInMarkdown: Boolean by BooleanPreference("KEY_BACKUP_MARKDOWN", false)
  var backupLockedNotes: Boolean by BooleanPreference("KEY_BACKUP_LOCKED", true)
  var performAutomaticBackups: Boolean by BooleanPreference("KEY_AUTO_BACKUP_MODE", false)
  var lastAutomaticBackup: Long
    get() = sharedPrefs.getLong("KEY_AUTO_BACKUP_LAST_TIMESTAMP", 0L)
    set(value) = sharedPrefs.edit { putLong("KEY_AUTO_BACKUP_LAST_TIMESTAMP", value) }
  
  companion object {
    const val DEFAULT_TEXT_SIZE = 16
  }

  private class BooleanPreference(private val preferenceKey: String, private val defaultValue: Boolean) {
    operator fun getValue(thisRef: AppPreferences, property: KProperty<*>): Boolean =
      thisRef.sharedPrefs.getBoolean(preferenceKey, defaultValue)

    operator fun setValue(thisRef: AppPreferences, property: KProperty<*>, value: Boolean) =
      thisRef.sharedPrefs.edit { putBoolean(preferenceKey, value) }
  }

  private class IntPreference(private val preferenceKey: String, private val defaultValue: Int) {
    operator fun getValue(thisRef: AppPreferences, property: KProperty<*>): Int =
      thisRef.sharedPrefs.getInt(preferenceKey, defaultValue)

    operator fun setValue(thisRef: AppPreferences, property: KProperty<*>, value: Int) =
      thisRef.sharedPrefs.edit { putInt(preferenceKey, value) }
  }

  private class StringPreference(private val preferenceKey: String, private val defaultValue: String) {
    operator fun getValue(thisRef: AppPreferences, property: KProperty<*>): String =
      thisRef.sharedPrefs.getString(preferenceKey, defaultValue)!!

    operator fun setValue(thisRef: AppPreferences, property: KProperty<*>, value: String) =
      thisRef.sharedPrefs.edit { putString(preferenceKey, value) }
  }
}