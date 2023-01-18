package com.maubis.scarlet.base.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.maubis.scarlet.base.database.daos.FolderDao
import com.maubis.scarlet.base.database.daos.NoteDao
import com.maubis.scarlet.base.database.daos.TagDao
import com.maubis.scarlet.base.database.daos.WidgetDao
import com.maubis.scarlet.base.database.entities.Folder
import com.maubis.scarlet.base.database.entities.Note
import com.maubis.scarlet.base.database.entities.Tag
import com.maubis.scarlet.base.database.entities.Widget

@Database(entities = [Note::class, Tag::class, Widget::class, Folder::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notes(): NoteDao
    abstract fun tags(): TagDao
    abstract fun folders(): FolderDao
    abstract fun widgets(): WidgetDao

    companion object {
        fun createDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "note-database")
                    .allowMainThreadQueries()
                    .build()
        }
    }
}