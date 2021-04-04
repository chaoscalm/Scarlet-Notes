package com.maubis.scarlet.base.database.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.maubis.scarlet.base.database.room.folder.Folder;
import com.maubis.scarlet.base.database.room.folder.FolderDao;
import com.maubis.scarlet.base.database.room.note.Note;
import com.maubis.scarlet.base.database.room.note.NoteDao;
import com.maubis.scarlet.base.database.room.tag.Tag;
import com.maubis.scarlet.base.database.room.tag.TagDao;
import com.maubis.scarlet.base.database.room.widget.Widget;
import com.maubis.scarlet.base.database.room.widget.WidgetDao;

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
