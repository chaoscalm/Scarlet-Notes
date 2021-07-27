package com.maubis.scarlet.base.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.maubis.scarlet.base.database.entities.Folder;

import java.util.List;

@Dao
public interface FolderDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insertFolder(Folder note);

  @Delete
  void delete(Folder note);

  @Query("SELECT * FROM folder ORDER BY timestamp DESC")
  List<Folder> getAll();
}
