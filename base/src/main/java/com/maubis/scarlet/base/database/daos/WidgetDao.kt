package com.maubis.scarlet.base.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.maubis.scarlet.base.database.entities.Widget;

import java.util.List;

@Dao
public interface WidgetDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insert(Widget widget);

  @Delete
  void delete(Widget tag);

  @Query("SELECT * FROM widget WHERE widgetId = :uid LIMIT 1")
  Widget getByID(int uid);

  @Query("SELECT * FROM widget WHERE noteUUID = :uuid")
  List<Widget> getByNote(String uuid);
}
