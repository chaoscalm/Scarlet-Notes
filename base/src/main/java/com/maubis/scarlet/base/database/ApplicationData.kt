package com.maubis.scarlet.base.database

import android.content.Context
import com.maubis.scarlet.base.database.daos.WidgetDao
import com.maubis.scarlet.base.notification.NotificationHandler

class ApplicationData(context: Context) {
  private val database: AppDatabase = AppDatabase.createDatabase(context)

  val notes = NotesRepository(database.notes(), NotificationHandler(context))
  val tags = TagsRepository(database.tags())
  val folders = FoldersRepository(database.folders())
  val widgets: WidgetDao = database.widgets()
}