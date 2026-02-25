package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanseva.ai.data.local.entity.InvestmentBreakdownEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentBreakdownDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreakdown(breakdown: InvestmentBreakdownEntity)

    @Query("SELECT * FROM investment_breakdowns WHERE id = :id")
    fun getBreakdownById(id: String): Flow<InvestmentBreakdownEntity?>

    @Query("SELECT * FROM investment_breakdowns WHERE cropId = :cropId")
    fun getBreakdownByCropId(cropId: String): Flow<InvestmentBreakdownEntity?>

    @Query("DELETE FROM investment_breakdowns WHERE id = :id")
    suspend fun deleteBreakdownById(id: String)
}
