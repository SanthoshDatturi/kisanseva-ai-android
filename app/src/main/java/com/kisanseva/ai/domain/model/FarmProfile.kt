package com.kisanseva.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SoilTexturePercentage(
    @SerialName("sand") val sand: Double,
    @SerialName("silt") val silt: Double,
    @SerialName("clay") val clay: Double
)

@Serializable
data class SoilTestProperties(
    @SerialName("soil_texture") val soilTexture: SoilTexturePercentage,
    @SerialName("ph_level") val phLevel: Double,
    @SerialName("electrical_conductivity_ds_m") val electricalConductivityDsM: Double,
    @SerialName("organic_carbon_percent") val organicCarbonPercent: Double,
    @SerialName("nitrogen_kg_per_acre") val nitrogenKgPerAcre: Double,
    @SerialName("phosphorus_kg_per_acre") val phosphorusKgPerAcre: Double,
    @SerialName("potassium_kg_per_acre") val potassiumKgPerAcre: Double,
    @SerialName("sulphur_ppm") val sulphurPpm: Double? = null,
    @SerialName("zinc_ppm") val zincPpm: Double? = null,
    @SerialName("boron_ppm") val boronPpm: Double? = null,
    @SerialName("iron_ppm") val ironPpm: Double? = null
)

@Serializable
enum class WaterSource {
    @SerialName("Well") WELL,
    @SerialName("Borewell") BOREWELL,
    @SerialName("Canal") CANAL,
    @SerialName("River") RIVER,
    @SerialName("Lake") LAKE,
    @SerialName("Rainwater Harvesting") RAINWATER_HARVESTING,
    @SerialName("Municipal Supply") MUNICIPAL_SUPPLY,
    @SerialName("Other") OTHER
}

@Serializable
enum class IrrigationSystem {
    @SerialName("Drip") DRIP,
    @SerialName("Sprinkler") SPRINKLER,
    @SerialName("Flood") FLOOD,
    @SerialName("Furrow") FURROW,
    @SerialName("Other") OTHER
}

@Serializable
enum class SoilType {
    @SerialName("Black soil") BLACK,
    @SerialName("Red soil") RED,
    @SerialName("Alluvial soil") ALLUVIAL,
    @SerialName("Laterite soil") LATERITE,
    @SerialName("Desert soil") DESERT,
    @SerialName("Forest soil") FOREST,
    @SerialName("Saline/Alkaline soil") SALINE,
    @SerialName("Sandy soil") SANDY,
    @SerialName("Clay soil") CLAY,
    @SerialName("Silty soil") SILTY,
    @SerialName("Loamy soil") LOAMY
}

@Serializable
data class Location(
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("village") val village: String,
    @SerialName("mandal") val mandal: String,
    @SerialName("district") val district: String,
    @SerialName("state") val state: String,
    @SerialName("zip_code") val zipCode: String
)

@Serializable
data class PreviousCrops(
    @SerialName("crop_name") val cropName: String,
    @SerialName("year") val year: Int,
    @SerialName("season") val season: String,
    @SerialName("yield_per_acre") val yieldPerAcre: String? = null,
    @SerialName("fertilizers_used") val fertilizersUsed: List<String>? = null,
    @SerialName("pesticides_used") val pesticidesUsed: List<String>? = null
)

@Serializable
data class FarmProfile(
    @SerialName("_id") val id: String,
    @SerialName("farmer_id") val farmerId: String,
    @SerialName("name") val name: String,
    @SerialName("location") val location: Location,
    @SerialName("soil_type") val soilType: SoilType,
    @SerialName("crops") val crops: List<PreviousCrops>? = null,
    @SerialName("total_area_acres") val totalAreaAcres: Double,
    @SerialName("cultivated_area_acres") val cultivatedAreaAcres: Double,
    @SerialName("soil_test_properties") val soilTestProperties: SoilTestProperties? = null,
    @SerialName("water_source") val waterSource: WaterSource,
    @SerialName("irrigation_system") val irrigationSystem: IrrigationSystem? = null
)
