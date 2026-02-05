package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanseva.ai.data.local.entity.SoilHealthRecommendationEntity

@Dao
interface SoilHealthRecommendationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(recommendation: SoilHealthRecommendationEntity)

    @Query("SELECT * FROM soil_health_recommendations WHERE id = :id")
    suspend fun getRecommendationById(id: String): SoilHealthRecommendationEntity?

    @Query("SELECT * FROM soil_health_recommendations WHERE cropId = :cropId")
    suspend fun getRecommendationsByCropId(cropId: String): List<SoilHealthRecommendationEntity>

    @Query("DELETE FROM soil_health_recommendations WHERE id = :id")
    suspend fun deleteRecommendationById(id: String)
}
