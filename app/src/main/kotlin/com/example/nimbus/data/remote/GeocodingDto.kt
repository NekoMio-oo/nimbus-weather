package com.example.nimbus.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Wire shapes for Open-Meteo's geocoding endpoint, which Nimbus still uses to turn a search string into a
// list of places. The uapis.cn weather service takes a city name, not coordinates, so this stays the only
// place the app talks to a second provider.

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