package com.maubis.scarlet.base.settings

import android.app.Dialog
import androidx.core.content.edit
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.common.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.common.sheets.LithoOptionsItem
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.home.MainActivity

var sEditorLiveMarkdown: Boolean
  get() = appPreferences.getBoolean("editor_live_markdown", true)
  set(value) = appPreferences.edit { putBoolean("editor_live_markdown", value) }

var sEditorMoveChecked: Boolean
  get() = appPreferences.getBoolean("editor_move_checked_items", false)
  set(value) = appPreferences.edit { putBoolean("editor_move_checked_items", value) }

var sEditorSkipNoteViewer: Boolean
  get() = appPreferences.getBoolean("skip_note_viewer", false)
  set(value) = appPreferences.edit { putBoolean("skip_note_viewer", value) }

var sNoteDefaultColor: Int
  get() = appPreferences.getInt("KEY_NOTE_DEFAULT_COLOR", (0xFFD32F2F).toInt())
  set(value) = appPreferences.edit { putInt("KEY_NOTE_DEFAULT_COLOR", value) }

class EditorOptionsBottomSheet : LithoOptionBottomSheet() {

  override fun title(): Int = R.string.home_option_editor_options_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val items = ArrayList<LithoOptionsItem>()
    val activity = context as MainActivity
    items.add(
      LithoOptionsItem(
        title = R.string.ui_options_note_background_color,
        subtitle = when (sUIUseNoteColorAsBackground) {
          true -> R.string.ui_options_note_background_color_settings_note
          false -> R.string.ui_options_note_background_color_settings_theme
        },
        icon = R.drawable.ic_color_picker,
        listener = {
          sUIUseNoteColorAsBackground = !sUIUseNoteColorAsBackground
          refresh(activity, dialog)
        },
        actionIcon = 0
      ))
    items.add(
      LithoOptionsItem(
        title = R.string.note_option_font_size,
        subtitle = 0,
        content = activity.getString(R.string.note_option_font_size_subtitle, sEditorTextSize),
        icon = R.drawable.ic_title,
        listener = {
          openSheet(activity, FontSizeBottomSheet())
          refresh(activity, dialog)
        },
        actionIcon = 0
      ))
    items.add(LithoOptionsItem(
      title = R.string.editor_option_enable_live_markdown,
      subtitle = R.string.editor_option_enable_live_markdown_description,
      icon = R.drawable.icon_realtime_markdown,
      selected = sEditorLiveMarkdown,
      isSelectable = true,
      listener = {
        sEditorLiveMarkdown = !sEditorLiveMarkdown
        refresh(componentContext.androidContext, dialog)
      }
    ))

    items.add(LithoOptionsItem(
      title = R.string.editor_option_skip_view_note,
      subtitle = R.string.editor_option_skip_view_note_details,
      icon = R.drawable.ic_redo,
      selected = sEditorSkipNoteViewer,
      isSelectable = true,
      listener = {
        sEditorSkipNoteViewer = !sEditorSkipNoteViewer
        refresh(componentContext.androidContext, dialog)
      }
    ))
    items.add(LithoOptionsItem(
      title = R.string.editor_option_move_checked_items,
      subtitle = R.string.editor_option_move_checked_items_description,
      icon = R.drawable.ic_checkbox,
      selected = sEditorMoveChecked,
      isSelectable = true,
      listener = {
        sEditorMoveChecked = !sEditorMoveChecked
        refresh(componentContext.androidContext, dialog)
      }
    ))
    return items
  }
}