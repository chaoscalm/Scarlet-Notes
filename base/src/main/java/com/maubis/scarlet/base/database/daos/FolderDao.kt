package com.maubis.scarlet.base.database.daos

import androidx.room.*
import com.maubis.scarlet.base.database.entities.Folder

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFolder(note: Folder)

    @Delete
    fun delete(note: Folder)

    @Query("SELECT * FROM folder ORDER BY timestamp DESC")
    fun getAll(): List<Folder>
}