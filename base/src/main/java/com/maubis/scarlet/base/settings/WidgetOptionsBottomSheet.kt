package com.maubis.scarlet.base.settings

import android.app.Application
import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.common.sheets.*
import com.maubis.scarlet.base.home.MainActivity
import com.maubis.scarlet.base.widget.NoteWidgetProvider
import com.maubis.scarlet.base.widget.RecentNotesWidgetProvider

class WidgetOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.home_option_widget_options_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(
      LithoOptionsItem(
        title = R.string.widget_option_show_locked_notes,
        subtitle = R.string.widget_option_show_locked_notes_details,
        icon = R.drawable.ic_lock,
        listener = {
          ScarletApp.prefs.showLockedNotesInWidgets = !ScarletApp.prefs.showLockedNotesInWidgets
          notifyWidgetConfigChanged(activity)
          refresh(activity, dialog)
        },
        isSelectable = true,
        selected = ScarletApp.prefs.showLockedNotesInWidgets
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.widget_option_show_archived_notes,
        subtitle = R.string.widget_option_show_archived_notes_details,
        icon = R.drawable.ic_archive,
        listener = {
          ScarletApp.prefs.showArchivedNotesInWidgets = !ScarletApp.prefs.showArchivedNotesInWidgets
          notifyWidgetConfigChanged(activity)
          refresh(activity, dialog)
        },
        isSelectable = true,
        selected = ScarletApp.prefs.showArchivedNotesInWidgets
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.widget_option_show_toolbar,
        subtitle = R.string.widget_option_show_toolbar_details,
        icon = R.drawable.ic_staggered_grid,
        listener = {
          ScarletApp.prefs.recentNotesWidgetShowToolbar = !ScarletApp.prefs.recentNotesWidgetShowToolbar
          notifyAllNotesConfigChanged(activity)
          refresh(activity, dialog)
        },
        isSelectable = true,
        selected = ScarletApp.prefs.recentNotesWidgetShowToolbar
      ))

    options.add(
      LithoOptionsItem(
        title = R.string.widget_option_background_color,
        subtitle = R.string.widget_option_background_color_details,
        icon = R.drawable.ic_color_picker,
        listener = {
          openSheet(activity, ColorPickerBottomSheet().apply {
            config = ColorPickerDefaultController(
              title = R.string.widget_option_background_color,
              selectedColor = ScarletApp.prefs.recentNotesWidgetBackground,
              colors = listOf(intArrayOf(Color.TRANSPARENT, 0x30000000, 0x65000000, 0xA0000000.toInt(), 0xC0000000.toInt())),
              onColorSelected = {
                ScarletApp.prefs.recentNotesWidgetBackground = it
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

    RecentNotesWidgetProvider.notifyAllChanged(activity)
    activity.sendBroadcast(singleNoteBroadcastIntent)
  }

  private fun notifyAllNotesConfigChanged(activity: MainActivity) {
    val application: Application = activity.applicationContext as Application
    val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
      ComponentName(application, RecentNotesWidgetProvider::class.java))

    val allNotesBroadcastIntent = Intent(application, RecentNotesWidgetProvider::class.java)
    allNotesBroadcastIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    allNotesBroadcastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    activity.sendBroadcast(allNotesBroadcastIntent)
  }
}