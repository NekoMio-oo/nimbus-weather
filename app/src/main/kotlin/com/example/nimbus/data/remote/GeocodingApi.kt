package com.example.nimbus.data.remote

import kotlinx.serialization.json.Json
import java.net.URLEncoder

/** The place search, as the repository sees it. */
interface GeocodingApi {
    suspend fun searchPlaces(query: String, count: Int = 8): GeocodingResponse
}

/**
 * Open-Meteo (https://open-meteo.com) still provides the geocoder: it is free, needs no key, and its
 * `language` parameter returns the city name in the reader's own script (a Chinese user searching "beijing"
 * gets "北京"), which is exactly the string the uapis.cn weather call then looks up.
 */
class OpenMeteoGeocodingApi(
    private val http: HttpClient,
    private val json: Json,
    private val language: () -> String = { "en" },
) : GeocodingApi {

    override suspend fun searchPlaces(query: String, count: Int): GeocodingResponse {
        val url = "$BASE/v1/search?name=${encode(query)}&count=$count" +
            "&language=${encode(language())}&format=json"
        return json.decodeFromString(GeocodingResponse.serializer(), http.get(url))
    }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")

    private companion object {
        const val BASE = "https://geocoding-api.open-meteo.com"
    }
}