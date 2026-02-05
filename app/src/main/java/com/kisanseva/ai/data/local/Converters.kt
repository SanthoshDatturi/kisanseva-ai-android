package com.kisanseva.ai.data.local

import androidx.room.TypeConverter
import com.kisanseva.ai.data.local.entity.PartEntity
import com.kisanseva.ai.domain.model.CropState
import com.kisanseva.ai.domain.model.CultivationTask
import com.kisanseva.ai.domain.model.CurrentWeatherResponse
import com.kisanseva.ai.domain.model.ForecastResponse
import com.kisanseva.ai.domain.model.GeocodingResponse
import com.kisanseva.ai.domain.model.ImmediateAction
import com.kisanseva.ai.domain.model.Investment
import com.kisanseva.ai.domain.model.IrrigationSystem
import com.kisanseva.ai.domain.model.PesticideInfo
import com.kisanseva.ai.domain.model.PesticideStage
import com.kisanseva.ai.domain.model.PesticideType
import com.kisanseva.ai.domain.model.PreviousCrops
import com.kisanseva.ai.domain.model.Profitability
import com.kisanseva.ai.domain.model.RiskFactor
import com.kisanseva.ai.domain.model.SoilTestProperties
import com.kisanseva.ai.domain.model.SoilTexturePercentage
import com.kisanseva.ai.domain.model.SoilType
import com.kisanseva.ai.domain.model.SpecificArrangement
import com.kisanseva.ai.domain.model.WaterSource
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromPartEntityList(value: List<PartEntity>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toPartEntityList(value: String?): List<PartEntity>? {
        return value?.let { json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromRiskFactorList(value: List<RiskFactor>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toRiskFactorList(value: String): List<RiskFactor> {
        return json.decodeFromString<List<RiskFactor>>(value)
    }

    @TypeConverter
    fun fromSpecificArrangementList(value: List<SpecificArrangement>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toSpecificArrangementList(value: String): List<SpecificArrangement> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromPreviousCropsList(value: List<PreviousCrops>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toPreviousCropsList(value: String?): List<PreviousCrops>? {
        return value?.let { json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromSoilTestProperties(value: SoilTestProperties?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toSoilTestProperties(value: String?): SoilTestProperties? {
        return value?.let { json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromSoilType(value: SoilType): String {
        return value.name
    }

    @TypeConverter
    fun toSoilType(value: String): SoilType {
        return SoilType.valueOf(value)
    }

    @TypeConverter
    fun fromWaterSource(value: WaterSource): String {
        return value.name
    }

    @TypeConverter
    fun toWaterSource(value: String): WaterSource {
        return WaterSource.valueOf(value)
    }

    @TypeConverter
    fun fromIrrigationSystem(value: IrrigationSystem?): String? {
        return value?.name
    }

    @TypeConverter
    fun toIrrigationSystem(value: String?): IrrigationSystem? {
        return value?.let { IrrigationSystem.valueOf(it) }
    }

    @TypeConverter
    fun fromSoilTexturePercentage(value: SoilTexturePercentage?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toSoilTexturePercentage(value: String?): SoilTexturePercentage? {
        return value?.let { json.decodeFromString<SoilTexturePercentage>(it) }
    }

    @TypeConverter
    fun fromCropState(value: CropState): String {
        return value.name
    }

    @TypeConverter
    fun toCropState(value: String): CropState {
        return CropState.valueOf(value)
    }

    @TypeConverter
    fun fromCurrentWeatherResponse(value: CurrentWeatherResponse): String {
        return json.encodeToString(CurrentWeatherResponse.serializer(), value)
    }

    @TypeConverter
    fun toCurrentWeatherResponse(value: String): CurrentWeatherResponse {
        return json.decodeFromString(CurrentWeatherResponse.serializer(), value)
    }

    @TypeConverter
    fun fromForecastResponse(value: ForecastResponse): String {
        return json.encodeToString(ForecastResponse.serializer(), value)
    }

    @TypeConverter
    fun toForecastResponse(value: String): ForecastResponse {
        return json.decodeFromString(ForecastResponse.serializer(), value)
    }

    @TypeConverter
    fun fromGeocodingResponseList(value: List<GeocodingResponse>): String {
        return json.encodeToString(ListSerializer(GeocodingResponse.serializer()), value)
    }

    @TypeConverter
    fun toGeocodingResponseList(value: String): List<GeocodingResponse> {
        return json.decodeFromString(ListSerializer(GeocodingResponse.serializer()), value)
    }

    @TypeConverter
    fun fromCultivationTaskList(value: List<CultivationTask>): String {
        return json.encodeToString(ListSerializer(CultivationTask.serializer()), value)
    }

    @TypeConverter
    fun toCultivationTaskList(value: String): List<CultivationTask> {
        return json.decodeFromString(ListSerializer(CultivationTask.serializer()), value)
    }

    @TypeConverter
    fun fromImmediateActionList(value: List<ImmediateAction>): String {
        return json.encodeToString(ListSerializer(ImmediateAction.serializer()), value)
    }

    @TypeConverter
    fun toImmediateActionList(value: String): List<ImmediateAction> {
        return json.decodeFromString(ListSerializer(ImmediateAction.serializer()), value)
    }

    @TypeConverter
    fun fromInvestmentList(value: List<Investment>): String {
        return json.encodeToString(ListSerializer(Investment.serializer()), value)
    }

    @TypeConverter
    fun toInvestmentList(value: String): List<Investment> {
        return json.decodeFromString(ListSerializer(Investment.serializer()), value)
    }

    @TypeConverter
    fun fromProfitability(value: Profitability): String {
        return json.encodeToString(Profitability.serializer(), value)
    }

    @TypeConverter
    fun toProfitability(value: String): Profitability {
        return json.decodeFromString(Profitability.serializer(), value)
    }

    @TypeConverter
    fun fromPesticideInfoList(value: List<PesticideInfo>): String {
        return json.encodeToString(ListSerializer(PesticideInfo.serializer()), value)
    }

    @TypeConverter
    fun toPesticideInfoList(value: String): List<PesticideInfo> {
        return json.decodeFromString(ListSerializer(PesticideInfo.serializer()), value)
    }

    @TypeConverter
    fun fromPesticideStage(value: PesticideStage): String {
        return value.name
    }

    @TypeConverter
    fun toPesticideStage(value: String): PesticideStage {
        return PesticideStage.valueOf(value)
    }

    @TypeConverter
    fun fromPesticideType(value: PesticideType): String {
        return value.name
    }

    @TypeConverter
    fun toPesticideType(value: String): PesticideType {
        return PesticideType.valueOf(value)
    }
}
