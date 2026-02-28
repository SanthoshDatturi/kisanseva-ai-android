package com.kisanseva.ai.data.repository

import com.kisanseva.ai.data.local.dao.FarmProfileDao
import com.kisanseva.ai.data.local.entity.FarmProfileEntity
import com.kisanseva.ai.data.remote.FarmApi
import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.FarmProfile
import com.kisanseva.ai.domain.repository.FarmRepository
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FarmRepositoryImpl(
    private val farmApi: FarmApi,
    private val farmProfileDao: FarmProfileDao
) : FarmRepository {

    override suspend fun createOrUpdateFarmProfile(profile: FarmProfile): Result<FarmProfile, DataError.Network> {
        return when (val result = farmApi.createOrUpdateFarmProfile(profile)) {
            is Result.Error -> Result.Error(result.error)
            is Result.Success -> {
                farmProfileDao.insertFarmProfile(farmProfileToEntity(result.data))
                Result.Success(result.data)
            }
        }
    }

    override fun getProfileById(farmId: String): Flow<FarmProfile?> {
        return farmProfileDao.getFarmProfileById(farmId).map { entity ->
            entity?.let { entityToFarmProfile(it) }
        }
    }

    override fun getFarmProfiles(): Flow<List<FarmProfile>> {
        return farmProfileDao.getFarmProfiles().map { entities ->
            entities.map { entityToFarmProfile(it) }
        }
    }

    override suspend fun refreshFarmProfiles(): Result<Unit, DataError.Network> {
        return when (val result = farmApi.getFarmProfiles()) {
            is Result.Error -> Result.Error(result.error)
            is Result.Success -> {
                result.data.forEach { farmProfileDao.insertFarmProfile(farmProfileToEntity(it)) }
                Result.Success(Unit)
            }
        }
    }

    override suspend fun refreshFarmProfileById(farmId: String): Result<Unit, DataError.Network> {
        return when (val result = farmApi.getProfileById(farmId)) {
            is Result.Error -> Result.Error(result.error)
            is Result.Success -> {
                farmProfileDao.insertFarmProfile(farmProfileToEntity(result.data))
                Result.Success(Unit)
            }
        }
    }

    override suspend fun deleteProfile(farmId: String): Result<Unit, DataError.Network> {
        return when (val result = farmApi.deleteProfile(farmId)) {
            is Result.Error -> Result.Error(result.error)
            is Result.Success -> {
                farmProfileDao.deleteFarmProfileById(farmId)
                Result.Success(Unit)
            }
        }
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
