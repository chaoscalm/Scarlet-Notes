package com.maubis.scarlet.base.settings

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.common.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.common.sheets.LithoOptionsItem
import com.maubis.scarlet.base.common.sheets.openSheet
import com.maubis.scarlet.base.home.MainActivity

class EditorOptionsBottomSheet : LithoOptionBottomSheet() {

  override fun title(): Int = R.string.home_option_editor_options_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val items = ArrayList<LithoOptionsItem>()
    val activity = context as MainActivity
    items.add(
      LithoOptionsItem(
        title = R.string.ui_options_note_background_color,
        subtitle = when (ScarletApp.prefs.useNoteColorAsBackground) {
          true -> R.string.ui_options_note_background_color_settings_note
          false -> R.string.ui_options_note_background_color_settings_theme
        },
        icon = R.drawable.ic_color_picker,
        listener = {
          ScarletApp.prefs.useNoteColorAsBackground = !ScarletApp.prefs.useNoteColorAsBackground
          refresh(activity, dialog)
        },
        actionIcon = 0
      ))
    items.add(
      LithoOptionsItem(
        title = R.string.note_option_font_size,
        subtitle = 0,
        content = activity.getString(R.string.note_option_font_size_subtitle, ScarletApp.prefs.editorTextSize),
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
      icon = R.drawable.ic_realtime_formatting,
      selected = ScarletApp.prefs.liveMarkdownInEditor,
      isSelectable = true,
      listener = {
        ScarletApp.prefs.liveMarkdownInEditor = !ScarletApp.prefs.liveMarkdownInEditor
        refresh(componentContext.androidContext, dialog)
      }
    ))

    items.add(LithoOptionsItem(
      title = R.string.editor_option_skip_view_note,
      subtitle = R.string.editor_option_skip_view_note_details,
      icon = R.drawable.ic_redo,
      selected = ScarletApp.prefs.skipNoteViewer,
      isSelectable = true,
      listener = {
        ScarletApp.prefs.skipNoteViewer = !ScarletApp.prefs.skipNoteViewer
        refresh(componentContext.androidContext, dialog)
      }
    ))
    items.add(LithoOptionsItem(
      title = R.string.editor_option_move_checked_items,
      subtitle = R.string.editor_option_move_checked_items_description,
      icon = R.drawable.ic_checkbox,
      selected = ScarletApp.prefs.moveCheckedItems,
      isSelectable = true,
      listener = {
        ScarletApp.prefs.moveCheckedItems = !ScarletApp.prefs.moveCheckedItems
        refresh(componentContext.androidContext, dialog)
      }
    ))
    return items
  }
}