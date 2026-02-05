package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.FarmProfileDao
import com.kisanseva.ai.data.local.entity.FarmProfileEntity
import com.kisanseva.ai.data.remote.FarmApi
import com.kisanseva.ai.domain.model.FarmProfile
import com.kisanseva.ai.domain.repository.FarmRepository

class FarmRepositoryImpl(
    private val farmApi: FarmApi,
    private val farmProfileDao: FarmProfileDao
) : FarmRepository {

    override suspend fun createOrUpdateFarmProfile(profile: FarmProfile): FarmProfile {
        val createdProfile = farmApi.createOrUpdateFarmProfile(profile)
        farmProfileDao.insertFarmProfile(farmProfileToEntity(createdProfile))
        return createdProfile
    }

    override suspend fun getProfileById(farmId: String): FarmProfile {
        val localProfile = farmProfileDao.getFarmProfileById(farmId)
        return if (localProfile != null) {
            entityToFarmProfile(localProfile)
        } else {
            val remoteProfile = farmApi.getProfileById(farmId)
            farmProfileDao.insertFarmProfile(farmProfileToEntity(remoteProfile))
            remoteProfile
        }
    }

    override suspend fun getFarmProfiles(): List<FarmProfile> {
        val localProfiles = farmProfileDao.getFarmProfiles()
        return if (localProfiles.isNotEmpty()) {
            localProfiles.map { entityToFarmProfile(it) }
        } else {
            val remoteProfiles = farmApi.getFarmProfiles()
            remoteProfiles.forEach { farmProfileDao.insertFarmProfile(farmProfileToEntity(it)) }
            remoteProfiles
        }
    }

    override suspend fun deleteProfile(farmId: String) {
        farmApi.deleteProfile(farmId)
        farmProfileDao.deleteFarmProfileById(farmId)
    }

    private fun farmProfileToEntity(profile: FarmProfile): FarmProfileEntity {
        return FarmProfileEntity(
            id = profile.id,
            farmerId = profile.farmerId,
            name = profile.name,
            location = profile.location,
            soilType = profile.soilType,
            crops = profile.crops,
            totalAreaAcres = profile.totalAreaAcres,
            cultivatedAreaAcres = profile.cultivatedAreaAcres,
            soilTestProperties = profile.soilTestProperties,
            waterSource = profile.waterSource,
            irrigationSystem = profile.irrigationSystem
        )
    }

    private fun entityToFarmProfile(entity: FarmProfileEntity): FarmProfile {
        return FarmProfile(
            id = entity.id,
            farmerId = entity.farmerId,
            name = entity.name,
            location = entity.location,
            soilType = entity.soilType,
            crops = entity.crops,
            totalAreaAcres = entity.totalAreaAcres,
            cultivatedAreaAcres = entity.cultivatedAreaAcres,
            soilTestProperties = entity.soilTestProperties,
            waterSource = entity.waterSource,
            irrigationSystem = entity.irrigationSystem
        )
    }
}
