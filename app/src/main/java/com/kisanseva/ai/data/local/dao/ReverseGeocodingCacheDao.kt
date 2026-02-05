package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanseva.ai.data.local.entity.ReverseGeocodingCacheEntity

@Dao
interface ReverseGeocodingCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ReverseGeocodingCacheEntity)

    @Query("SELECT * FROM reverse_geocoding_cache WHERE id = :id")
    suspend fun get(id: String): ReverseGeocodingCacheEntity?
}
