package com.maubis.scarlet.base.database.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Underlying Database, difficult to migrate to Kotlin without breaking the Database.
 * Hence all the functions should be in NoteKExtensions
 */
@Entity(tableName = "note", indices = {@Index("uid")})
public class Note {
  @PrimaryKey(autoGenerate = true)
  public int uid;

  public String description = "";

  public Long timestamp;

  public Integer color;

  public String state;

  public boolean locked;

  public String tags = "";

  public long updateTimestamp;

  public boolean pinned;

  public String uuid;

  public String meta;

  public boolean disableBackup;

  public String folder = "";

}
