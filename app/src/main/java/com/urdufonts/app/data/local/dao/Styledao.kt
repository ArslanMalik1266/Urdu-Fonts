package com.urdufonts.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.urdufonts.app.data.local.entity.StyleEntity

@Dao
interface StyleDao {
    @Query("SELECT * FROM styles")
    suspend fun getAll(): List<StyleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(styles: List<StyleEntity>)

    @Query("DELETE FROM styles")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM styles")
    suspend fun count(): Int
}