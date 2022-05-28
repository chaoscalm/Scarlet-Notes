package com.maubis.scarlet.base.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.maubis.scarlet.base.ScarletApp

class NoteWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    for (appWidgetId in appWidgetIds) {
      val widget = ScarletApp.data.widgets.getByID(appWidgetId) ?: continue
      WidgetConfigureActivity.createNoteWidget(context, widget)
    }
  }

  override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
    super.onDeleted(context, appWidgetIds)

    if (appWidgetIds == null) {
      return
    }

    for (appWidgetId in appWidgetIds) {
      val widget = ScarletApp.data.widgets.getByID(appWidgetId)
      if (widget === null) {
        continue
      }
      ScarletApp.data.widgets.delete(widget)
    }
  }

}