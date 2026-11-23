package com.urdufonts.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.urdufonts.app.data.local.entity.FontEntity

@Dao
interface FontDao {
    @Query("SELECT * FROM fonts")
    suspend fun getAll(): List<FontEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fonts: List<FontEntity>)

    @Query("DELETE FROM fonts")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM fonts")
    suspend fun count(): Int
}