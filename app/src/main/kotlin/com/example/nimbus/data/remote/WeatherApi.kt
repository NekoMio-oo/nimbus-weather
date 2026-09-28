package com.example.nimbus.data.remote

import kotlinx.serialization.json.Json
import java.net.URLEncoder

/** The remote weather service, as the repositories see it. */
interface WeatherApi {
    suspend fun searchPlaces(query: String, count: Int = 8): GeocodingResponse

    suspend fun forecast(latitude: Double, longitude: Double): ForecastResponse
}

/**
 * Open-Meteo (https://open-meteo.com): free for non-commercial use, no API key. The forecast query asks for
 * exactly the fields the DTOs declare, with times in the place's own zone (`timezone=auto`).
 */
class OpenMeteoApi(
    private val http: HttpClient,
    private val json: Json,
) : WeatherApi {

    override suspend fun searchPlaces(query: String, count: Int): GeocodingResponse {
        val url = "$GEOCODING_BASE/v1/search?name=${encode(query)}&count=$count&language=en&format=json"
        return json.decodeFromString(GeocodingResponse.serializer(), http.get(url))
    }

    override suspend fun forecast(latitude: Double, longitude: Double): ForecastResponse {
        val url = buildString {
            append(FORECAST_BASE).append("/v1/forecast")
            append("?latitude=").append(latitude)
            append("&longitude=").append(longitude)
            append("&current=").append(CURRENT_FIELDS)
            append("&hourly=").append(HOURLY_FIELDS)
            append("&daily=").append(DAILY_FIELDS)
            append("&timezone=auto&forecast_days=7&forecast_hours=24")
        }
        return json.decodeFromString(ForecastResponse.serializer(), http.get(url))
    }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")

    private companion object {
        const val GEOCODING_BASE = "https://geocoding-api.open-meteo.com"
        const val FORECAST_BASE = "https://api.open-meteo.com"
        const val CURRENT_FIELDS = "temperature_2m,relative_humidity_2m,apparent_temperature,is_day," +
            "precipitation,weather_code,wind_speed_10m,wind_direction_10m,surface_pressure,uv_index"
        const val HOURLY_FIELDS = "temperature_2m,weather_code,precipitation_probability"
        const val DAILY_FIELDS = "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset," +
            "precipitation_probability_max,uv_index_max"
    }
}
