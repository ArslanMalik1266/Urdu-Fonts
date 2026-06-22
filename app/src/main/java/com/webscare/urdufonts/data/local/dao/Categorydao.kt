package com.webscare.urdufonts.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.webscare.urdufonts.data.local.entity.CategoryEntity

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    suspend fun getAll(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}