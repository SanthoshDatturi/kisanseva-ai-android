package com.kisanseva.ai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kisanseva.ai.domain.model.SpecificArrangement

@Entity(tableName = "intercropping_details")
data class InterCroppingDetailsEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo("intercrop_type") val intercropType: String,
    @ColumnInfo("no_of_crops") val noOfCrops: Int,
    val arrangement: String,
    @ColumnInfo("specific_arrangement") val specificArrangement: List<SpecificArrangement>,
    val benefits: List<String>
)
