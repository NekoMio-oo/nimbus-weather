package com.example.nimbus.data.remote

import com.example.nimbus.domain.model.ForecastException
import com.example.nimbus.domain.model.Place
import kotlinx.serialization.json.Json
import java.net.URLEncoder

/** The weather service, as the repository sees it: one JSON answer per place. */
interface WeatherApi {
    suspend fun forecast(place: Place, language: String): WeatherResponse
}

/**
 * UApiPro (https://uapis.cn), a Chinese weather aggregator. Its `/misc/weather` endpoint is asked for every
 * optional block at once — extended metrics, the 7-day forecast, the 24-hour forecast, the minute-level
 * precipitation and the life indices — so a single request fills the whole screen. The service locates by
 * city name or by Chinese `adcode`, not by coordinates, and returns the city's own local wall-clock times,
 * so the place's IANA zone is only needed to label them.
 *
 * The service's name index is narrower than the geocoder's, so a name the app knows can still answer 404
 * `LOCATION_NOT_FOUND`. When that happens the place is resolved through `/misc/district` — first the nearest
 * known area to the coordinates, then a keyword match on the name — and the forecast is asked again with the
 * canonical `adcode` (China) or name (everywhere else), before giving up as [ForecastException.LocationNotFound].
 *
 * [language] is `zh` or `en`; anything else the app ships falls back to `en`.
 */
class UapiWeatherApi(
    private val http: HttpClient,
    private val json: Json,
    private val apiKey: String,
) : WeatherApi {

    override suspend fun forecast(place: Place, language: String): WeatherResponse {
        weather(city = place.name, adcode = null, language = language)?.let { return it }
        for (candidate in candidates(place)) {
            weather(city = candidate.name, adcode = candidate.adcode, language = language)?.let { return it }
        }
        throw ForecastException.LocationNotFound()
    }

    /** One `/misc/weather` call, or null when the service has no such place. */
    private suspend fun weather(city: String?, adcode: String?, language: String): WeatherResponse? {
        val url = buildString {
            append(BASE).append("/api/v1/misc/weather?")
            if (adcode.isNullOrBlank()) append("city=").append(encode(city.orEmpty()))
            else append("adcode=").append(encode(adcode))
            append("&extended=true&forecast=true&hourly=true&minutely=true&indices=true")
            append("&lang=").append(encode(language))
            append("&key=").append(encode(apiKey))
        }
        return try {
            json.decodeFromString(WeatherResponse.serializer(), http.get(url))
        } catch (e: HttpException) {
            if (e.statusCode == HTTP_NOT_FOUND) null else throw e
        }
    }

    /**
     * Places worth retrying with when the exact name misses: the nearest area to the coordinates (an `adcode`
     * first, since that is an exact administrative match, then the nearest name as a rougher guess), then the
     * first keyword match on the name, which fixes only a spelling or translation mismatch.
     */
    private suspend fun candidates(place: Place): List<DistrictArea> = buildList {
        if (place.latitude != 0.0 || place.longitude != 0.0) {
            val nearby = districts("lat=${place.latitude}&lng=${place.longitude}&limit=10")
            nearby.firstOrNull { !it.adcode.isNullOrBlank() }?.let { add(it) }
            nearby.firstOrNull()?.let { add(it) }
        }
        districts("keywords=${encode(place.name)}&limit=1").firstOrNull()?.let { add(it) }
    }.distinctBy { it.adcode ?: it.name }

    /** One `/misc/district` lookup. An empty list means the lookup itself failed; it never fails the load. */
    private suspend fun districts(query: String): List<DistrictArea> {
        val url = "$BASE/api/v1/misc/district?$query"
        return try {
            json.decodeFromString(DistrictResponse.serializer(), http.get(url)).results
        } catch (e: HttpException) {
            emptyList()
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")

    private companion object {
        const val BASE = "https://uapis.cn"
        const val HTTP_NOT_FOUND = 404
    }
}