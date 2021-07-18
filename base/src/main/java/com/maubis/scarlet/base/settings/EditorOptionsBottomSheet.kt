package com.maubis.scarlet.base.settings

import android.app.Dialog
import androidx.core.content.edit
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.openSheet

var sEditorLiveMarkdown: Boolean
  get() = appPreferences.getBoolean("editor_live_markdown", true)
  set(value) = appPreferences.edit { putBoolean("editor_live_markdown", value) }

var sEditorMoveChecked: Boolean
  get() = appPreferences.getBoolean("editor_move_checked_items", true)
  set(value) = appPreferences.edit { putBoolean("editor_move_checked_items", value) }

var sEditorMarkdownDefault: Boolean
  get() = appPreferences.getBoolean("editor_markdown_default", false)
  set(value) = appPreferences.edit { putBoolean("editor_markdown_default", value) }

var sEditorSkipNoteViewer: Boolean
  get() = appPreferences.getBoolean("skip_note_viewer", false)
  set(value) = appPreferences.edit { putBoolean("skip_note_viewer", value) }

var sEditorMoveHandles: Boolean
  get() = appPreferences.getBoolean("editor_move_handles", true)
  set(value) = appPreferences.edit { putBoolean("editor_move_handles", value) }

var sEditorMarkdownEnabled: Boolean
  get() = appPreferences.getBoolean("KEY_MARKDOWN_ENABLED", true)
  set(value) = appPreferences.edit { putBoolean("KEY_MARKDOWN_ENABLED", value) }

var sNoteDefaultColor: Int
  get() = appPreferences.getInt("KEY_NOTE_DEFAULT_COLOR", (0xFFD32F2F).toInt())
  set(value) = appPreferences.edit { putInt("KEY_NOTE_DEFAULT_COLOR", value) }

class EditorOptionsBottomSheet : LithoOptionBottomSheet() {

  override fun title(): Int = R.string.home_option_editor_options_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val items = ArrayList<LithoOptionsItem>()
    val activity = context as MainActivity
    items.add(LithoOptionsItem(
      title = R.string.note_option_default_color,
      subtitle = R.string.note_option_default_color_subtitle,
      icon = R.drawable.ic_action_color,
      listener = {
        val config = ColorPickerDefaultController(
          title = R.string.note_option_default_color,
          colors = listOf(
            activity.resources.getIntArray(R.array.bright_colors),
            activity.resources.getIntArray(R.array.bright_colors_accent)),
          selectedColor = sNoteDefaultColor,
          onColorSelected = { sNoteDefaultColor = it }
        )
        openSheet(activity, ColorPickerBottomSheet().apply { this.config = config })
        dismiss()
      }
    ))
    items.add(LithoOptionsItem(
      title = R.string.markdown_sheet_markdown_support,
      subtitle = R.string.markdown_sheet_markdown_support_subtitle,
      icon = R.drawable.ic_markdown_logo,
      selected = sEditorMarkdownEnabled,
      isSelectable = true,
      listener = {
        sEditorMarkdownEnabled = !sEditorMarkdownEnabled
        reset(componentContext.androidContext, dialog)
      }
    ))
    items.add(LithoOptionsItem(
      title = R.string.editor_option_enable_live_markdown,
      subtitle = R.string.editor_option_enable_live_markdown_description,
      icon = R.drawable.icon_realtime_markdown,
      selected = sEditorLiveMarkdown,
      isSelectable = true,
      listener = {
        sEditorLiveMarkdown = !sEditorLiveMarkdown
        reset(componentContext.androidContext, dialog)
      }
    ))
    items.add(LithoOptionsItem(
      title = R.string.editor_option_enable_markdown_mode_default,
      subtitle = R.string.editor_option_enable_markdown_mode_default_details,
      icon = R.drawable.ic_formats_logo,
      selected = sEditorMarkdownDefault,
      isSelectable = true,
      listener = {
        sEditorMarkdownDefault = !sEditorMarkdownDefault
        reset(componentContext.androidContext, dialog)
      }
    ))

    items.add(LithoOptionsItem(
      title = R.string.editor_option_skip_view_note,
      subtitle = R.string.editor_option_skip_view_note_details,
      icon = R.drawable.ic_redo_history,
      selected = sEditorSkipNoteViewer,
      isSelectable = true,
      listener = {
        sEditorSkipNoteViewer = !sEditorSkipNoteViewer
        reset(componentContext.androidContext, dialog)
      }
    ))
    items.add(LithoOptionsItem(
      title = R.string.editor_option_move_checked_items,
      subtitle = R.string.editor_option_move_checked_items_description,
      icon = R.drawable.ic_check_box_white_24dp,
      selected = sEditorMoveChecked,
      isSelectable = true,
      listener = {
        sEditorMoveChecked = !sEditorMoveChecked
        reset(componentContext.androidContext, dialog)
      }
    ))
    items.add(LithoOptionsItem(
      title = R.string.editor_option_enable_move_handle,
      subtitle = R.string.editor_option_enable_move_handle_description,
      icon = R.drawable.icon_drag_indicator,
      selected = sEditorMoveHandles,
      isSelectable = true,
      listener = {
        sEditorMoveHandles = !sEditorMoveHandles
        reset(componentContext.androidContext, dialog)
      }
    ))
    return items
  }
}