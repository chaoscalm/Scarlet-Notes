package com.maubis.scarlet.base.settings

import android.app.Application
import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import androidx.core.content.edit
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp.Companion.appPreferences
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.widget.AllNotesWidgetProvider
import com.maubis.scarlet.base.widget.NoteWidgetProvider

const val STORE_KEY_WIDGET_SHOW_LOCKED_NOTES = "widget_show_locked_notes"
var sWidgetShowLockedNotes: Boolean
  get() = appPreferences.getBoolean(STORE_KEY_WIDGET_SHOW_LOCKED_NOTES, false)
  set(value) = appPreferences.edit { putBoolean(STORE_KEY_WIDGET_SHOW_LOCKED_NOTES, value) }

const val STORE_KEY_WIDGET_SHOW_ARCHIVED_NOTES = "widget_show_archived_notes"
var sWidgetShowArchivedNotes: Boolean
  get() = appPreferences.getBoolean(STORE_KEY_WIDGET_SHOW_ARCHIVED_NOTES, true)
  set(value) = appPreferences.edit { putBoolean(STORE_KEY_WIDGET_SHOW_ARCHIVED_NOTES, value) }

const val STORE_KEY_WIDGET_BACKGROUND_COLOR = "widget_background_color_v1"
var sWidgetBackgroundColor: Int
  get() = appPreferences.getInt(STORE_KEY_WIDGET_BACKGROUND_COLOR, 0x65000000)
  set(value) = appPreferences.edit { putInt(STORE_KEY_WIDGET_BACKGROUND_COLOR, value) }

const val STORE_KEY_WIDGET_SHOW_TOOLBAR = "widget_show_toolbar"
var sWidgetShowToolbar: Boolean
  get() = appPreferences.getBoolean(STORE_KEY_WIDGET_SHOW_TOOLBAR, true)
  set(value) = appPreferences.edit { putBoolean(STORE_KEY_WIDGET_SHOW_TOOLBAR, value) }

class WidgetOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.home_option_widget_options_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(
      LithoOptionsItem(
        title = R.string.widget_option_show_locked_notes,
        subtitle = R.string.widget_option_show_locked_notes_details,
        icon = R.drawable.ic_action_lock,
        listener = {
          sWidgetShowLockedNotes = !sWidgetShowLockedNotes
          notifyWidgetConfigChanged(activity)
          refresh(activity, dialog)
        },
        isSelectable = true,
        selected = sWidgetShowLockedNotes
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.widget_option_show_archived_notes,
        subtitle = R.string.widget_option_show_archived_notes_details,
        icon = R.drawable.ic_archive_white_48dp,
        listener = {
          sWidgetShowArchivedNotes = !sWidgetShowArchivedNotes
          notifyWidgetConfigChanged(activity)
          refresh(activity, dialog)
        },
        isSelectable = true,
        selected = sWidgetShowArchivedNotes
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.widget_option_show_toolbar,
        subtitle = R.string.widget_option_show_toolbar_details,
        icon = R.drawable.ic_action_grid,
        listener = {
          sWidgetShowToolbar = !sWidgetShowToolbar
          notifyAllNotesConfigChanged(activity)
          refresh(activity, dialog)
        },
        isSelectable = true,
        selected = sWidgetShowToolbar
      ))

    options.add(
      LithoOptionsItem(
        title = R.string.widget_option_background_color,
        subtitle = R.string.widget_option_background_color_details,
        icon = R.drawable.ic_action_color,
        listener = {
          openSheet(activity, ColorPickerBottomSheet().apply {
            config = ColorPickerDefaultController(
              title = R.string.widget_option_background_color,
              selectedColor = sWidgetBackgroundColor,
              colors = listOf(intArrayOf(Color.TRANSPARENT, Color.WHITE, Color.LTGRAY, 0x65000000, Color.DKGRAY, Color.BLACK)),
              onColorSelected = {
                sWidgetBackgroundColor = it
                notifyAllNotesConfigChanged(activity)
              },
              columns = 6)
          })
        },
        actionIcon = 0
      ))
    return options
  }

  private fun notifyWidgetConfigChanged(activity: MainActivity) {
    val application: Application = activity.applicationContext as Application
    val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
      ComponentName(application, NoteWidgetProvider::class.java))

    val singleNoteBroadcastIntent = Intent(application, NoteWidgetProvider::class.java)
    singleNoteBroadcastIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    singleNoteBroadcastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

    AllNotesWidgetProvider.notifyAllChanged(activity)
    activity.sendBroadcast(singleNoteBroadcastIntent)
  }

  private fun notifyAllNotesConfigChanged(activity: MainActivity) {
    val application: Application = activity.applicationContext as Application
    val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
      ComponentName(application, AllNotesWidgetProvider::class.java))

    val allNotesBroadcastIntent = Intent(application, AllNotesWidgetProvider::class.java)
    allNotesBroadcastIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    allNotesBroadcastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    activity.sendBroadcast(allNotesBroadcastIntent)
  }
}