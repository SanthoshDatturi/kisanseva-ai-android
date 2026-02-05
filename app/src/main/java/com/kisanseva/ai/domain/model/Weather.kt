package com.kisanseva.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalNames(
    val ascii: String? = null,
    @SerialName("feature_name")
    val featureName: String? = null
)

@Serializable
data class GeocodingResponse(
    val name: String,
    @SerialName("local_names")
    val localNames: LocalNames? = null,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
)

@Serializable
data class WeatherCondition(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

@Serializable
data class MainWeatherData(
    val temp: Double,
    @SerialName("feels_like")
    val feelsLike: Double,
    @SerialName("temp_min")
    val tempMin: Double,
    @SerialName("temp_max")
    val tempMax: Double,
    val pressure: Int,
    val humidity: Int,
    @SerialName("sea_level")
    val seaLevel: Int? = null,
    @SerialName("grnd_level")
    val grndLevel: Int? = null
)

@Serializable
data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double? = null
)

@Serializable
data class Clouds(
    val all: Int
)

@Serializable
data class Rain(
    @SerialName("1h")
    val oneHour: Double? = null,
    @SerialName("3h")
    val threeHours: Double? = null
)

@Serializable
data class Snow(
    @SerialName("1h")
    val oneHour: Double? = null,
    @SerialName("3h")
    val threeHours: Double? = null
)

@Serializable
data class Sys(
    val type: Int? = null,
    val id: Int? = null,
    val country: String? = null,
    val sunrise: String? = null,
    val sunset: String? = null
)

@Serializable
data class CurrentWeatherResponse(
    val weather: List<WeatherCondition>,
    val main: MainWeatherData,
    val visibility: Int,
    val wind: Wind,
    val clouds: Clouds,
    val rain: Rain? = null,
    val snow: Snow? = null,
    val dt: String,
    val sys: Sys,
    val timezone: Int,
    val id: Int,
    val name: String
)

@Serializable
data class ForecastListItem(
    val dt: String,
    val main: MainWeatherData,
    val weather: List<WeatherCondition>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Int,
    val pop: Double,
    val rain: Rain? = null,
    val snow: Snow? = null,
    @SerialName("dt_txt")
    val dtTxt: String
)

@Serializable
data class Coordinates(
    val lat: Double,
    val lon: Double
)

@Serializable
data class City(
    val id: Int,
    val name: String,
    val coord: Coordinates,
    val country: String,
    val population: Int,
    val timezone: Int,
    val sunrise: String,
    val sunset: String
)

@Serializable
data class ForecastResponse(
    val list: List<ForecastListItem>,
    val city: City
)
