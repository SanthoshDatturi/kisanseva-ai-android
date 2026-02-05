package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanseva.ai.data.local.entity.PesticideRecommendationEntity

@Dao
interface PesticideRecommendationDao {
    @Query("SELECT * FROM pesticide_recommendation WHERE id = :id")
    suspend fun getRecommendationById(id: String): PesticideRecommendationEntity?

    @Query("SELECT * FROM pesticide_recommendation WHERE cropId = :cropId ORDER BY timestamp DESC")
    suspend fun getRecommendationsByCropId(cropId: String): List<PesticideRecommendationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(recommendation: PesticideRecommendationEntity)

    @Query("DELETE FROM pesticide_recommendation WHERE id = :id")
    suspend fun deleteRecommendation(id: String)
}
