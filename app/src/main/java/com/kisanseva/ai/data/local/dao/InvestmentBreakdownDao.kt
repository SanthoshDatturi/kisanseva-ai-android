package com.kisanseva.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanseva.ai.data.local.entity.InvestmentBreakdownEntity

@Dao
interface InvestmentBreakdownDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreakdown(breakdown: InvestmentBreakdownEntity)

    @Query("SELECT * FROM investment_breakdowns WHERE id = :id")
    suspend fun getBreakdownById(id: String): InvestmentBreakdownEntity?

    @Query("SELECT * FROM investment_breakdowns WHERE cropId = :cropId")
    suspend fun getBreakdownByCropId(cropId: String): InvestmentBreakdownEntity?

    @Query("DELETE FROM investment_breakdowns WHERE id = :id")
    suspend fun deleteBreakdownById(id: String)
}
