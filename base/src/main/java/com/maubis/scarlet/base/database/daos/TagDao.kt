package com.maubis.scarlet.base.database.daos

import androidx.room.*
import com.maubis.scarlet.base.database.entities.Tag

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTag(tag: Tag): Long

    @Delete
    fun delete(tag: Tag)

    @Query("SELECT count(*) FROM tag")
    fun getCount(): Int

    @Query("SELECT * FROM tag ORDER BY uid")
    fun getAll(): List<Tag>

    @Query("SELECT * FROM tag WHERE uid = :uid LIMIT 1")
    fun getByID(uid: Int): Tag

    @Query("SELECT * FROM tag WHERE uuid = :uuid LIMIT 1")
    fun getByUUID(uuid: String): Tag
}