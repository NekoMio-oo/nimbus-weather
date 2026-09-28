package com.example.nimbus.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Wire shapes for the two Open-Meteo endpoints Nimbus calls. Field names follow the API, so a change in the
// query string (see OpenMeteoApi) is mirrored here and nowhere else. Every list is nullable-element because
// the service reports gaps as JSON null.

@Serializable
data class GeocodingResponse(
    val results: List<GeocodingResult> = emptyList(),
)

@Serializable
data class GeocodingResult(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    val admin1: String? = null,
    val timezone: String? = null,
    val population: Long? = null,
)

@Serializable
data class ForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentDto,
    val hourly: HourlyDto,
    val daily: DailyDto,
)

@Serializable
data class CurrentDto(
    val time: String,
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("relative_humidity_2m") val humidity: Int = 0,
    @SerialName("apparent_temperature") val apparentTemperature: Double,
    @SerialName("is_day") val isDay: Int = 1,
    val precipitation: Double = 0.0,
    @SerialName("weather_code") val weatherCode: Int = 3,
    @SerialName("wind_speed_10m") val windSpeed: Double = 0.0,
    @SerialName("wind_direction_10m") val windDirection: Int = 0,
    @SerialName("surface_pressure") val surfacePressure: Double = 0.0,
    @SerialName("uv_index") val uvIndex: Double? = null,
)

@Serializable
data class HourlyDto(
    val time: List<String>,
    @SerialName("temperature_2m") val temperature: List<Double?>,
    @SerialName("weather_code") val weatherCode: List<Int?>,
    @SerialName("precipitation_probability") val precipitationProbability: List<Int?> = emptyList(),
)

@Serializable
data class DailyDto(
    val time: List<String>,
    @SerialName("weather_code") val weatherCode: List<Int?>,
    @SerialName("temperature_2m_max") val temperatureMax: List<Double?>,
    @SerialName("temperature_2m_min") val temperatureMin: List<Double?>,
    val sunrise: List<String?>,
    val sunset: List<String?>,
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: List<Int?> = emptyList(),
    @SerialName("uv_index_max") val uvIndexMax: List<Double?> = emptyList(),
)

/** A forecast response as stored on disk, with when it was fetched. */
@Serializable
data class CachedForecast(
    val fetchedAtEpochMs: Long,
    val response: ForecastResponse,
)
