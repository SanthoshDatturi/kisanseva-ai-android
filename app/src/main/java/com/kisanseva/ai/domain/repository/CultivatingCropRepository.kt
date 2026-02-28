package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.domain.model.IntercroppingDetails
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.Flow

interface CultivatingCropRepository {
    fun getCultivatingCropsByFarmId(farmId: String): Flow<List<CultivatingCrop>>
    fun getAllCultivatingCrops(): Flow<List<CultivatingCrop>>
    fun getCultivatingCropById(cultivatingCropId: String): Flow<CultivatingCrop?>
    suspend fun deleteCultivatingCrop(cultivatingCropId: String): Result<Unit, DataError.Network>
    fun getIntercroppingDetailsById(intercroppingDetailsId: String): Flow<IntercroppingDetails?>
    suspend fun saveCultivatingCrop(crop: CultivatingCrop)
    suspend fun saveIntercroppingDetails(details: IntercroppingDetails)
    suspend fun refreshCultivatingCropsByFarmId(farmId: String): Result<Unit, DataError.Network>
    suspend fun refreshAllCultivatingCrops(): Result<Unit, DataError.Network>
    suspend fun refreshCultivatingCropById(cultivatingCropId: String): Result<Unit, DataError.Network>
    suspend fun refreshIntercroppingDetailsById(intercroppingDetailsId: String): Result<Unit, DataError.Network>
}
