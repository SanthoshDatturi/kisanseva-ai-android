package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.CultivatingCropDao
import com.kisanseva.ai.data.local.dao.InterCroppingDetailsDao
import com.kisanseva.ai.data.local.entity.CultivatingCropEntity
import com.kisanseva.ai.data.local.entity.InterCroppingDetailsEntity
import com.kisanseva.ai.data.remote.CultivatingCropApi
import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.domain.model.IntercroppingDetails
import com.kisanseva.ai.domain.repository.CultivatingCropRepository
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CultivatingCropRepositoryImpl(
    private val cultivatingCropApi: CultivatingCropApi,
    private val cultivatingCropDao: CultivatingCropDao,
    private val interCroppingDetailsDao: InterCroppingDetailsDao
) : CultivatingCropRepository {

    override fun getAllCultivatingCrops(): Flow<List<CultivatingCrop>> {
        return cultivatingCropDao.getAllCultivatingCrops().map { entities ->
            entities.map { entityToCultivatingCrop(it) }
        }
    }

    override fun getCultivatingCropsByFarmId(farmId: String): Flow<List<CultivatingCrop>> {
        return cultivatingCropDao.getCultivatingCropsByFarmId(farmId).map { entities ->
            entities.map { entityToCultivatingCrop(it) }
        }
    }

    override fun getCultivatingCropById(cultivatingCropId: String): Flow<CultivatingCrop?> {
        return cultivatingCropDao.getCultivatingCropById(cultivatingCropId).map { entity ->
            entity?.let { entityToCultivatingCrop(it) }
        }
    }

    override fun getIntercroppingDetailsById(intercroppingDetailsId: String): Flow<IntercroppingDetails?> {
        return interCroppingDetailsDao.getInterCroppingDetailsById(intercroppingDetailsId).map { entity ->
            entity?.let { entityToIntercroppingDetails(it) }
        }
    }

    override suspend fun refreshCultivatingCropsByFarmId(farmId: String): Result<Unit, DataError.Network> {
        return when (val result = cultivatingCropApi.getCultivatingCropsByFarmId(farmId)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                result.data.forEach { cultivatingCropDao.insertCultivatingCrop(cultivatingCropToEntity(it)) }
                Result.Success<Unit, DataError.Network>(Unit)
            }
        }
    }

    override suspend fun refreshAllCultivatingCrops(): Result<Unit, DataError.Network> {
        return when (val result = cultivatingCropApi.getAllCultivatingCrops()) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                result.data.forEach { cultivatingCropDao.insertCultivatingCrop(cultivatingCropToEntity(it)) }
                Result.Success<Unit, DataError.Network>(Unit)
            }
        }
    }

    override suspend fun refreshCultivatingCropById(cultivatingCropId: String): Result<Unit, DataError.Network> {
        return when (val result = cultivatingCropApi.getCultivatingCropById(cultivatingCropId)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                cultivatingCropDao.insertCultivatingCrop(cultivatingCropToEntity(result.data))
                Result.Success<Unit, DataError.Network>(Unit)
            }
        }
    }

    override suspend fun refreshIntercroppingDetailsById(intercroppingDetailsId: String): Result<Unit, DataError.Network> {
        return when (val result = cultivatingCropApi.getIntercroppingDetailsById(intercroppingDetailsId)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                interCroppingDetailsDao.insertOrUpdate(interCroppingDetailsToEntity(result.data))
                Result.Success<Unit, DataError.Network>(Unit)
            }
        }
    }

    override suspend fun deleteCultivatingCrop(cultivatingCropId: String): Result<Unit, DataError.Network> {
        return when (val result = cultivatingCropApi.deleteCultivatingCrop(cultivatingCropId)) {
            is Result.Error -> Result.Error<Unit, DataError.Network>(result.error)
            is Result.Success -> {
                cultivatingCropDao.deleteCultivatingCropById(cultivatingCropId)
                Result.Success<Unit, DataError.Network>(Unit)
            }
        }
    }

    override suspend fun saveCultivatingCrop(crop: CultivatingCrop) {
        cultivatingCropDao.insertCultivatingCrop(cultivatingCropToEntity(crop))
    }

    override suspend fun saveIntercroppingDetails(details: IntercroppingDetails) {
        interCroppingDetailsDao.insertOrUpdate(interCroppingDetailsToEntity(details))
    }

    private fun interCroppingDetailsToEntity(details: IntercroppingDetails): InterCroppingDetailsEntity {
        return InterCroppingDetailsEntity(
            id = details.id,
            intercropType = details.intercropType,
            noOfCrops = details.noOfCrops,
            arrangement = details.arrangement,
            specificArrangement = details.specificArrangement,
            benefits = details.benefits
        )
    }

    private fun entityToIntercroppingDetails(entity: InterCroppingDetailsEntity): IntercroppingDetails {
        return IntercroppingDetails(
            id = entity.id,
            intercropType = entity.intercropType,
            noOfCrops = entity.noOfCrops,
            arrangement = entity.arrangement,
            specificArrangement = entity.specificArrangement,
            benefits = entity.benefits
        )
    }

    private fun cultivatingCropToEntity(crop: CultivatingCrop): CultivatingCropEntity {
        return CultivatingCropEntity(
            id = crop.id,
            farmId = crop.farmId,
            name = crop.name,
            variety = crop.variety,
            imageUrl = crop.imageUrl,
            cropState = crop.cropState,
            description = crop.description,
            intercroppingId = crop.intercroppingId
        )
    }

    private fun entityToCultivatingCrop(entity: CultivatingCropEntity): CultivatingCrop {
        return CultivatingCrop(
            id = entity.id,
            farmId = entity.farmId,
            name = entity.name,
            variety = entity.variety,
            imageUrl = entity.imageUrl,
            cropState = entity.cropState,
            description = entity.description,
            intercroppingId = entity.intercroppingId
        )
    }
}
