package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kisanseva.ai.data.local.entity.CropRecommendationEntity
import com.kisanseva.ai.data.local.entity.CropRecommendationWithRelations
import com.kisanseva.ai.data.local.entity.InterCropRecommendationEntity
import com.kisanseva.ai.data.local.entity.InterCropRecommendationWithRelations
import com.kisanseva.ai.data.local.entity.MonoCropEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropRecommendationDao {

    @Transaction
    @Query("SELECT * FROM crop_recommendation WHERE farmId = :farmId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestCropRecommendation(farmId: String): Flow<CropRecommendationWithRelations?>

    @Transaction
    @Query("SELECT * FROM crop_recommendation WHERE id = :recommendationId")
    fun getCropRecommendationById(recommendationId: String): Flow<CropRecommendationWithRelations?>

    @Query("SELECT * FROM mono_crop WHERE id = :monoCropId")
    fun getMonoCropById(monoCropId: String): Flow<MonoCropEntity?>

    @Transaction
    @Query("SELECT * FROM inter_crop_recommendation WHERE id = :interCropId")
    fun getInterCropById(interCropId: String): Flow<InterCropRecommendationWithRelations?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCropRecommendation(cropRecommendation: CropRecommendationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonoCrops(monoCrops: List<MonoCropEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterCrops(interCrops: List<InterCropRecommendationEntity>)

    @Transaction
    suspend fun insertCropRecommendationWithRelations(
        cropRecommendation: CropRecommendationEntity,
        monoCrops: List<MonoCropEntity>,
        interCrops: List<InterCropRecommendationEntity>
    ) {
        insertCropRecommendation(cropRecommendation)
        insertMonoCrops(monoCrops)
        insertInterCrops(interCrops)
    }
}
