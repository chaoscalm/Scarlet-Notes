package com.maubis.scarlet.base.database

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.maubis.scarlet.base.database.daos.WidgetDao

class ApplicationData(context: Context) {
  private val database: AppDatabase = AppDatabase.createDatabase(context)

  val notes = NotesRepository(database.notes(), context.getSystemService<NotificationManager>()!!)
  val tags = TagsRepository(database.tags())
  val folders = FoldersRepository(database.folders())
  val widgets: WidgetDao = database.widgets()
}