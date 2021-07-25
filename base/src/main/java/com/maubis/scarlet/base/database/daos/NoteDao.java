package com.maubis.scarlet.base.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.maubis.scarlet.base.database.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insertNote(Note note);

  @Delete
  void delete(Note note);

  @Query("SELECT * FROM note ORDER BY pinned DESC, timestamp DESC")
  List<Note> getAll();
}
