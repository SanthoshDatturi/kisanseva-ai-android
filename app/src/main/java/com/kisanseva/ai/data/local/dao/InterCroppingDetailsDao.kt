package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanseva.ai.data.local.entity.InterCroppingDetailsEntity

@Dao
interface InterCroppingDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(interCroppingDetails: InterCroppingDetailsEntity)

    @Query("SELECT * FROM intercropping_details WHERE id = :id")
    suspend fun getInterCroppingDetailsById(id: String): InterCroppingDetailsEntity?
}
