package com.maubis.scarlet.base.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.maubis.scarlet.base.database.daos.FolderDao;
import com.maubis.scarlet.base.database.daos.NoteDao;
import com.maubis.scarlet.base.database.daos.TagDao;
import com.maubis.scarlet.base.database.daos.WidgetDao;
import com.maubis.scarlet.base.database.entities.Folder;
import com.maubis.scarlet.base.database.entities.Note;
import com.maubis.scarlet.base.database.entities.Tag;
import com.maubis.scarlet.base.database.entities.Widget;

@Database(entities = {Note.class, Tag.class, Widget.class, Folder.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
  public static AppDatabase createDatabase(Context context) {
    return Room.databaseBuilder(context, AppDatabase.class, "note-database")
               .allowMainThreadQueries()
               .build();
  }

  public abstract NoteDao notes();

  public abstract TagDao tags();

  public abstract FolderDao folders();

  public abstract WidgetDao widgets();
}
