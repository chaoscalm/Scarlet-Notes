package com.maubis.scarlet.base.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.TaskStackBuilder
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.editor.CreateListNoteActivity
import com.maubis.scarlet.base.editor.EditNoteActivity
import com.maubis.scarlet.base.home.MainActivity

class QuickActionsWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    for (appWidgetId in appWidgetIds) {
      val views = RemoteViews(context.packageName, R.layout.widget_quick_actions)
      views.setOnClickPendingIntent(R.id.add_note, getPendingIntent(context, EditNoteActivity::class.java, 23100))

      val pendingIntentList = getPendingIntent(context, CreateListNoteActivity::class.java, 23101)
      views.setOnClickPendingIntent(R.id.add_list, pendingIntentList)

      val intentApp = Intent(context, MainActivity::class.java)
      val pendingIntentApp = PendingIntent.getActivity(context, 23102, intentApp, 0)
      views.setOnClickPendingIntent(R.id.open_app, pendingIntentApp)

      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }

  private fun <T> getPendingIntent(context: Context, activityClass: Class<T>, requestCode: Int): PendingIntent {
    return getPendingIntentWithStack(context, requestCode, Intent(context, activityClass))
  }
}

fun getPendingIntentWithStack(
  context: Context, requestCode: Int, resultIntent: Intent, flags: Int = PendingIntent.FLAG_UPDATE_CURRENT): PendingIntent {
  return TaskStackBuilder.create(context)
    .addNextIntentWithParentStack(Intent(context, MainActivity::class.java))
    .addNextIntent(resultIntent)
    .getPendingIntent(requestCode, flags)
    ?: PendingIntent.getActivity(context, requestCode, resultIntent, 0)
}