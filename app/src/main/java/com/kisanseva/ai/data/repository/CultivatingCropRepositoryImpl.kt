package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.CultivatingCropDao
import com.kisanseva.ai.data.local.dao.InterCroppingDetailsDao
import com.kisanseva.ai.data.local.entity.CultivatingCropEntity
import com.kisanseva.ai.data.local.entity.InterCroppingDetailsEntity
import com.kisanseva.ai.data.remote.CultivatingCropApi
import com.kisanseva.ai.domain.model.CultivatingCrop
import com.kisanseva.ai.domain.model.IntercroppingDetails
import com.kisanseva.ai.domain.repository.CultivatingCropRepository

class CultivatingCropRepositoryImpl(
    private val cultivatingCropApi: CultivatingCropApi,
    private val cultivatingCropDao: CultivatingCropDao,
    private val interCroppingDetailsDao: InterCroppingDetailsDao
) : CultivatingCropRepository {

    override suspend fun getAllCultivatingCrops(): List<CultivatingCrop> {
        return cultivatingCropDao.getAllCultivatingCrops().map { entityToCultivatingCrop(it) }
    }

    override suspend fun getCultivatingCropsByFarmId(farmId: String): List<CultivatingCrop> {
        val localCrops = cultivatingCropDao.getCultivatingCropsByFarmId(farmId)
        return if (localCrops.isNotEmpty()) {
            localCrops.map { entityToCultivatingCrop(it) }
        } else {
            val remoteCrops = cultivatingCropApi.getCultivatingCropsByFarmId(farmId)
            remoteCrops.forEach { cultivatingCropDao.insertCultivatingCrop(cultivatingCropToEntity(it)) }
            remoteCrops
        }
    }

    override suspend fun getCultivatingCropById(cultivatingCropId: String): CultivatingCrop {
        val localCrop = cultivatingCropDao.getCultivatingCropById(cultivatingCropId)
        return if (localCrop != null) {
            entityToCultivatingCrop(localCrop)
        } else {
            val remoteCrop = cultivatingCropApi.getCultivatingCropById(cultivatingCropId)
            cultivatingCropDao.insertCultivatingCrop(cultivatingCropToEntity(remoteCrop))
            remoteCrop
        }
    }

    override suspend fun deleteCultivatingCrop(cultivatingCropId: String) {
        cultivatingCropApi.deleteCultivatingCrop(cultivatingCropId)
        cultivatingCropDao.deleteCultivatingCropById(cultivatingCropId)
    }

    override suspend fun getIntercroppingDetailsById(intercroppingDetailsId: String): IntercroppingDetails {
        val localDetails = interCroppingDetailsDao.getInterCroppingDetailsById(intercroppingDetailsId)
        return if (localDetails != null) {
            entityToIntercroppingDetails(localDetails)
        } else {
            val remoteDetails = cultivatingCropApi.getIntercroppingDetailsById(intercroppingDetailsId)
            interCroppingDetailsDao.insertOrUpdate(interCroppingDetailsToEntity(remoteDetails))
            remoteDetails
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
