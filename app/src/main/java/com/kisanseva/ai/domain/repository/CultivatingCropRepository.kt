package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.domain.model.IntercroppingDetails

interface CultivatingCropRepository {
    suspend fun getCultivatingCropsByFarmId(farmId: String): List<CultivatingCrop>
    suspend fun getAllCultivatingCrops(): List<CultivatingCrop>
    suspend fun getCultivatingCropById(cultivatingCropId: String): CultivatingCrop
    suspend fun deleteCultivatingCrop(cultivatingCropId: String)
    suspend fun getIntercroppingDetailsById(intercroppingDetailsId: String): IntercroppingDetails
    suspend fun saveCultivatingCrop(crop: CultivatingCrop)
    suspend fun saveIntercroppingDetails(details: IntercroppingDetails)
}
