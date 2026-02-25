package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.FarmProfile
import kotlinx.coroutines.flow.Flow

interface FarmRepository {
    suspend fun createOrUpdateFarmProfile(profile: FarmProfile): FarmProfile
    fun getProfileById(farmId: String): Flow<FarmProfile?>
    fun getFarmProfiles(): Flow<List<FarmProfile>>
    suspend fun deleteProfile(farmId: String)
    suspend fun refreshFarmProfiles()
    suspend fun refreshFarmProfileById(farmId: String)
}