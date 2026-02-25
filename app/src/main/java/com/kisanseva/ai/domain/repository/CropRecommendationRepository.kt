package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.CropRecommendationResponse
import com.kisanseva.ai.domain.model.InterCropRecommendation
import com.kisanseva.ai.domain.model.MonoCrop
import kotlinx.coroutines.flow.Flow

interface CropRecommendationRepository {

    fun getCropRecommendationByFarmId(farmId: String): Flow<CropRecommendationResponse?>

    fun getCropRecommendationById(recommendationId: String): Flow<CropRecommendationResponse?>

    fun getMonoCropById(monoCropId: String): Flow<MonoCrop?>

    fun getInterCropById(interCropId: String): Flow<InterCropRecommendation?>

    suspend fun requestCropRecommendation(farmId: String)

    fun listenToCropRecommendations(): Flow<CropRecommendationResponse>

    fun getLatestCropRecommendation(farmId: String): Flow<CropRecommendationResponse?>

    suspend fun selectCropForCultivation(
        cropId: String,
        farmId: String,
        cropRecommendationResponseId: String
    )

    suspend fun refreshCropRecommendationByFarmId(farmId: String)
    suspend fun refreshCropRecommendationById(recommendationId: String)
}
