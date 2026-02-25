package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanseva.ai.data.local.entity.FarmProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmProfile(profile: FarmProfileEntity)

    @Query("SELECT * FROM farm_profiles WHERE id = :farmId")
    fun getFarmProfileById(farmId: String): Flow<FarmProfileEntity?>

    @Query("SELECT * FROM farm_profiles")
    fun getFarmProfiles(): Flow<List<FarmProfileEntity>>

    @Query("DELETE FROM farm_profiles WHERE id = :farmId")
    suspend fun deleteFarmProfileById(farmId: String)

    @Query("DELETE FROM farm_profiles")
    suspend fun deleteAllFarmProfiles()
}
