package com.kisanseva.ai.domain.repository

import com.kisanseva.ai.domain.model.FarmProfile

interface FarmRepository {
    suspend fun createOrUpdateFarmProfile(profile: FarmProfile): FarmProfile
    suspend fun getProfileById(farmId: String): FarmProfile
    suspend fun getFarmProfiles(): List<FarmProfile>
    suspend fun deleteProfile(farmId: String)
}