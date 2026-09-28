package com.example.nimbus.data.remote

import kotlinx.serialization.json.Json
import java.net.URLEncoder

/** The weather service, as the repository sees it: one JSON answer per city. */
interface WeatherApi {
    suspend fun forecast(city: String, language: String): WeatherResponse
}

/**
 * UApiPro (https://uapis.cn), a Chinese weather aggregator. Its `/misc/weather` endpoint is asked for every
 * optional block at once — extended metrics, the 7-day forecast, the 24-hour forecast, the minute-level
 * precipitation and the life indices — so a single request fills the whole screen. The service locates by
 * city name (`adcode` is the other option), not coordinates, and returns the city's own local wall-clock
 * times, so the place's IANA zone is only needed to label them.
 *
 * [language] is `zh` or `en`; anything else the app ships falls back to `en`.
 */
class UapiWeatherApi(
    private val http: HttpClient,
    private val json: Json,
    private val apiKey: String,
) : WeatherApi {

    override suspend fun forecast(city: String, language: String): WeatherResponse {
        val url = buildString {
            append(BASE).append("/api/v1/misc/weather")
            append("?city=").append(encode(city))
            append("&extended=true&forecast=true&hourly=true&minutely=true&indices=true")
            append("&lang=").append(encode(language))
            append("&key=").append(encode(apiKey))
        }
        return json.decodeFromString(WeatherResponse.serializer(), http.get(url))
    }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")

    private companion object {
        const val BASE = "https://uapis.cn"
    }
}