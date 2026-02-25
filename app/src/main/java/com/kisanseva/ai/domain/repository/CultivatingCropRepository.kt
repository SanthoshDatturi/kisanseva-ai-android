package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.domain.model.IntercroppingDetails
import kotlinx.coroutines.flow.Flow

interface CultivatingCropRepository {
    fun getCultivatingCropsByFarmId(farmId: String): Flow<List<CultivatingCrop>>
    fun getAllCultivatingCrops(): Flow<List<CultivatingCrop>>
    fun getCultivatingCropById(cultivatingCropId: String): Flow<CultivatingCrop?>
    suspend fun deleteCultivatingCrop(cultivatingCropId: String)
    fun getIntercroppingDetailsById(intercroppingDetailsId: String): Flow<IntercroppingDetails?>
    suspend fun saveCultivatingCrop(crop: CultivatingCrop)
    suspend fun saveIntercroppingDetails(details: IntercroppingDetails)
    suspend fun refreshCultivatingCropsByFarmId(farmId: String)
    suspend fun refreshAllCultivatingCrops()
    suspend fun refreshCultivatingCropById(cultivatingCropId: String)
    suspend fun refreshIntercroppingDetailsById(intercroppingDetailsId: String)
}
