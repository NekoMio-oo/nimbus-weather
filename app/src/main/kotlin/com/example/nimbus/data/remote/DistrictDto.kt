package com.example.nimbus.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * UApiPro's `/misc/district`: an administrative-area lookup over the same service that serves the weather.
 * Nimbus uses it only to turn a place the weather endpoint does not recognise into one it does — its own
 * geocoder stays Open-Meteo — so this is deliberately a thin slice of the response.
 */
@Serializable
data class DistrictResponse(
    val total: Int = 0,
    val results: List<DistrictArea> = emptyList(),
)

@Serializable
data class DistrictArea(
    val name: String,
    val level: String? = null,
    val country: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    val province: String? = null,
    val city: String? = null,
    val district: String? = null,
    /** Chinese administrative code, e.g. `440300`. Absent for places outside China. */
    val adcode: String? = null,
    val citycode: String? = null,
    val timezone: String? = null,
    val population: Long? = null,
)