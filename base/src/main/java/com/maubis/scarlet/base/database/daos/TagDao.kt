package com.maubis.scarlet.base.database.daos

import androidx.room.*
import com.maubis.scarlet.base.database.entities.Tag

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTag(tag: Tag)

    @Delete
    fun delete(tag: Tag)

    @Query("SELECT count(*) FROM tag")
    fun getCount(): Int

    @Query("SELECT * FROM tag")
    fun getAll(): List<Tag>
}