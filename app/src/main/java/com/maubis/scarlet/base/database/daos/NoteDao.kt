package com.maubis.scarlet.base.database.daos

import androidx.room.*
import com.maubis.scarlet.base.database.entities.Note

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: Note): Long

    @Delete
    fun delete(note: Note)

    @Query("SELECT * FROM note ORDER BY pinned DESC, timestamp DESC")
    fun getAll(): List<Note>
}