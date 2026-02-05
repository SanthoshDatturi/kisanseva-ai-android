package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanseva.ai.data.local.entity.CurrentWeatherCacheEntity

@Dao
interface CurrentWeatherCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CurrentWeatherCacheEntity)

    @Query("SELECT * FROM current_weather_cache WHERE id = :id")
    suspend fun get(id: String): CurrentWeatherCacheEntity?
}
