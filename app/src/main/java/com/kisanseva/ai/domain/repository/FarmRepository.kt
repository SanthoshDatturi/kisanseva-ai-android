package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.error.DataError
import com.kisanseva.ai.domain.model.FarmProfile
import com.kisanseva.ai.domain.state.Result
import kotlinx.coroutines.flow.Flow

interface FarmRepository {
    suspend fun createOrUpdateFarmProfile(profile: FarmProfile): Result<FarmProfile, DataError.Network>
    fun getProfileById(farmId: String): Flow<FarmProfile?>
    fun getFarmProfiles(): Flow<List<FarmProfile>>
    suspend fun deleteProfile(farmId: String): Result<Unit, DataError.Network>
    suspend fun refreshFarmProfiles(): Result<Unit, DataError.Network>
    suspend fun refreshFarmProfileById(farmId: String): Result<Unit, DataError.Network>
}
