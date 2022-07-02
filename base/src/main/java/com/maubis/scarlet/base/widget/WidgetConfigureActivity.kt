package com.maubis.scarlet.base.widget

import android.app.Activity
import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.ScarletApp
import com.maubis.scarlet.base.ScarletApp.Companion.data
import com.maubis.scarlet.base.common.utils.ColorUtil
import com.maubis.scarlet.base.common.utils.sort
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.Widget
import com.maubis.scarlet.base.editor.ViewNoteActivity
import com.maubis.scarlet.base.note.getTextForWidget
import com.maubis.scarlet.base.note.selection.INoteSelectorActivity
import com.maubis.scarlet.base.note.selection.SelectableNotesActivityBase

class WidgetConfigureActivity : SelectableNotesActivityBase(), INoteSelectorActivity {

  var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_select_note)

    val intent = intent
    val extras = intent.extras
    if (extras != null) {
      appWidgetId = extras.getInt(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
      return
    }

    initUI()
  }

  override fun getNotes(): List<Note> {
    return sort(getAvailableNotesForWidgets(), ScarletApp.prefs.notesSortingTechnique)
  }

  override fun onNoteClicked(note: Note) {
    val widget = Widget(appWidgetId, note.uuid)
    data.widgets.insert(widget)
    createWidget(widget)
  }

  override fun isNoteSelected(note: Note): Boolean {
    return true
  }

  private fun createWidget(widget: Widget) {
    createNoteWidget(this, widget)

    val resultValue = Intent()
    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget.widgetId)
    setResult(Activity.RESULT_OK, resultValue)
    finish()
  }

  companion object {
    fun createNoteWidget(context: Context, widget: Widget) {
      val note = data.notes.getByUUID(widget.noteUuid)
      val appWidgetManager = AppWidgetManager.getInstance(context)
      if (note === null || (note.locked && !ScarletApp.prefs.showLockedNotesInWidgets)) {
        val views = RemoteViews(context.packageName, R.layout.widget_invalid_note)
        appWidgetManager.updateAppWidget(widget.widgetId, views)
        return
      }

      val pendingIntent = ViewNoteActivity.makePendingIntentWithStack(context, note)
      val views = RemoteViews(context.packageName, R.layout.widget_note)

      views.setTextViewText(R.id.description, note.getTextForWidget())
      views.setInt(R.id.container_layout, "setBackgroundColor", note.color)

      val isLightShaded = ColorUtil.isLightColor(note.color)
      val colorResource = if (isLightShaded) {
        com.github.bijoysingh.uibasics.R.color.dark_tertiary_text
      } else {
        com.github.bijoysingh.uibasics.R.color.light_secondary_text
      }
      val textColor = ContextCompat.getColor(context, colorResource)
      views.setInt(R.id.description, "setTextColor", textColor)

      views.setOnClickPendingIntent(R.id.description, pendingIntent)
      views.setOnClickPendingIntent(R.id.container_layout, pendingIntent)

      appWidgetManager.updateAppWidget(widget.widgetId, views)
    }

    private fun notifyNoteChangeBroadcast(context: Context, note: Note): Intent? {
      val application: Application = context.applicationContext as Application
      val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
        ComponentName(application, NoteWidgetProvider::class.java))

      val widgets = data.widgets.getByNote(note.uuid)
      val widgetIds = ArrayList<Int>()
      for (widget in widgets) {
        if (ids.contains(widget.widgetId)) {
          widgetIds.add(widget.widgetId)
        }
      }

      if (widgetIds.isEmpty()) {
        return null
      }

      val intent = Intent(application, NoteWidgetProvider::class.java)
      intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds.toIntArray())
      return intent
    }

    fun notifyNoteChange(context: Context?, note: Note?) {
      if (context === null || note === null) {
        return
      }

      val intent = notifyNoteChangeBroadcast(context, note)
      if (intent === null) {
        return
      }
      context.sendBroadcast(intent)
    }
  }
}
