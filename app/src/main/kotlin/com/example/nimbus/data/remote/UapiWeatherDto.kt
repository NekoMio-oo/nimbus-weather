package com.example.nimbus.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Wire shapes for https://uapis.cn/api/v1/misc/weather (UApiPro). The query string decides which optional
// blocks come back (see UapiWeatherApi), so every block is nullable and every scalar carries a default: a
// city outside China has no `minutely_precip`, and a leaner answer may drop the forecast or indices. Field
// names follow the API, so a change in the query is mirrored here and nowhere else.

@Serializable
data class WeatherResponse(
    val province: String? = null,
    val city: String? = null,
    val district: String? = null,
    val adcode: String? = null,
    val weather: String = "",
    @SerialName("weather_icon") val weatherIcon: String? = null,
    val temperature: Double = 0.0,
    @SerialName("wind_direction") val windDirection: String? = null,
    @SerialName("wind_power") val windPower: String? = null,
    val humidity: Int = 0,
    @SerialName("report_time") val reportTime: String? = null,

    @SerialName("temp_max") val tempMax: Double? = null,
    @SerialName("temp_min") val tempMin: Double? = null,
    val forecast: List<DailyForecastDto>? = null,

    @SerialName("feels_like") val feelsLike: Double? = null,
    val visibility: Double? = null,
    val pressure: Double? = null,
    val uv: Double? = null,
    val aqi: Int? = null,
    @SerialName("aqi_level") val aqiLevel: Int? = null,
    @SerialName("aqi_category") val aqiCategory: String? = null,
    @SerialName("aqi_primary") val aqiPrimary: String? = null,
    @SerialName("air_pollutants") val airPollutants: AirPollutantsDto? = null,
    val precipitation: Double? = null,
    val cloud: Int? = null,
    @SerialName("life_indices") val lifeIndices: Map<String, LifeIndexDto>? = null,
    @SerialName("hourly_forecast") val hourlyForecast: List<HourlyForecastDto>? = null,
    @SerialName("minutely_precip") val minutelyPrecip: MinutelyDto? = null,
    @SerialName("minutely_forecast") val minutelyForecast: MinutelyDto? = null,
)

@Serializable
data class DailyForecastDto(
    val date: String,
    val week: String? = null,
    @SerialName("temp_max") val tempMax: Double = 0.0,
    @SerialName("temp_min") val tempMin: Double = 0.0,
    @SerialName("weather_day") val weatherDay: String? = null,
    @SerialName("weather_night") val weatherNight: String? = null,
    @SerialName("wind_dir_day") val windDirDay: String? = null,
    @SerialName("wind_dir_night") val windDirNight: String? = null,
    @SerialName("wind_scale_day") val windScaleDay: String? = null,
    @SerialName("wind_scale_night") val windScaleNight: String? = null,
    @SerialName("wind_speed_day") val windSpeedDay: Double? = null,
    val humidity: Int? = null,
    val precip: Double? = null,
    val pop: Int? = null,
    val cloud: Int? = null,
    @SerialName("uv_index") val uvIndex: Double? = null,
    val sunrise: String? = null,
    val sunset: String? = null,
)

@Serializable
data class HourlyForecastDto(
    val time: String,
    val temperature: Double = 0.0,
    val weather: String? = null,
    @SerialName("wind_direction") val windDirection: String? = null,
    @SerialName("wind_speed") val windSpeed: Double? = null,
    @SerialName("wind_scale") val windScale: String? = null,
    val humidity: Int? = null,
    val precip: Double? = null,
    @SerialName("feels_like") val feelsLike: Double? = null,
    val visibility: Double? = null,
    val pop: Int? = null,
    val pressure: Double? = null,
    val cloud: Int? = null,
    @SerialName("uv_index") val uvIndex: Double? = null,
)

/** Both `minutely_precip` and `minutely_forecast` carry this shape; either may be absent outside China. */
@Serializable
data class MinutelyDto(
    val summary: String? = null,
    @SerialName("update_time") val updateTime: String? = null,
    val data: List<MinutelyPointDto> = emptyList(),
)

@Serializable
data class MinutelyPointDto(
    val time: String,
    val precip: Double = 0.0,
    val type: String? = null,
)

@Serializable
data class LifeIndexDto(
    val level: String? = null,
    val brief: String? = null,
    val advice: String? = null,
)

@Serializable
data class AirPollutantsDto(
    val pm25: Double? = null,
    val pm10: Double? = null,
    val o3: Double? = null,
    val no2: Double? = null,
    val so2: Double? = null,
    val co: Double? = null,
)

/** A forecast response as stored on disk, with when it was fetched. */
@Serializable
data class CachedForecast(
    val fetchedAtEpochMs: Long,
    val response: WeatherResponse,
)