package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kisanseva.ai.data.local.entity.CultivatingCropEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CultivatingCropDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCultivatingCrop(crop: CultivatingCropEntity)

    @Update
    suspend fun updateCultivatingCrop(crop: CultivatingCropEntity)

    @Query("DELETE FROM cultivating_crop WHERE id = :cropId")
    suspend fun deleteCultivatingCropById(cropId: String)

    @Query("DELETE FROM cultivating_crop")
    suspend fun deleteAllCultivatingCrops()

    @Query("SELECT * FROM cultivating_crop WHERE farm_id = :farmId")
    fun getCultivatingCropsByFarmId(farmId: String): Flow<List<CultivatingCropEntity>>

    @Query("SELECT * FROM cultivating_crop WHERE id = :cropId")
    fun getCultivatingCropById(cropId: String): Flow<CultivatingCropEntity?>

    @Query("SELECT * FROM cultivating_crop")
    fun getAllCultivatingCrops(): Flow<List<CultivatingCropEntity>>
}
