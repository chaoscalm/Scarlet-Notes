package com.maubis.scarlet.base.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.maubis.scarlet.base.database.entities.Tag;

import java.util.List;

@Dao
public interface TagDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insertTag(Tag tag);

  @Delete
  void delete(Tag tag);

  @Query("SELECT count(*) FROM tag")
  int getCount();

  @Query("SELECT * FROM tag ORDER BY uid")
  List<Tag> getAll();

  @Query("SELECT * FROM tag WHERE uid = :uid LIMIT 1")
  Tag getByID(int uid);

  @Query("SELECT * FROM tag WHERE uuid = :uuid LIMIT 1")
  Tag getByUUID(String uuid);
}
