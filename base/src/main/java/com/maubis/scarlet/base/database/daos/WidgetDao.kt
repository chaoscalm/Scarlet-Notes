package com.maubis.scarlet.base.database.daos

import androidx.room.*
import com.maubis.scarlet.base.database.entities.Widget
import java.util.*

@Dao
interface WidgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(widget: Widget): Long

    @Delete
    fun delete(tag: Widget)

    @Query("SELECT * FROM widget WHERE widgetId = :uid LIMIT 1")
    fun getByID(uid: Int): Widget?

    @Query("SELECT * FROM widget WHERE noteUuid = :uuid")
    fun getByNote(uuid: UUID): List<Widget>
}