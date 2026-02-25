package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanseva.ai.data.local.entity.PesticideRecommendationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PesticideRecommendationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(recommendation: PesticideRecommendationEntity)

    @Query("SELECT * FROM pesticide_recommendations WHERE id = :recommendationId")
    fun getRecommendationById(recommendationId: String): Flow<PesticideRecommendationEntity?>

    @Query("SELECT * FROM pesticide_recommendations WHERE cropId = :cropId")
    fun getRecommendationsByCropId(cropId: String): Flow<List<PesticideRecommendationEntity>>

    @Query("DELETE FROM pesticide_recommendations WHERE id = :recommendationId")
    suspend fun deleteRecommendation(recommendationId: String)
}
