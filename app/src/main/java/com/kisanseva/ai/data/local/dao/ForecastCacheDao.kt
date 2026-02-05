package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanseva.ai.data.local.entity.ForecastCacheEntity

@Dao
interface ForecastCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ForecastCacheEntity)

    @Query("SELECT * FROM forecast_cache WHERE id = :id")
    suspend fun get(id: String): ForecastCacheEntity?
}
