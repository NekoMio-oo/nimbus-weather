package com.example.nimbus.domain.model

/**
 * Air quality for the place: the headline index plus, when the service reports them, the pollutant
 * concentrations it was derived from.
 */
data class AirQuality(
    val aqi: Int,
    /** The service's 1–6 band, used for the colour when it gives no [category] text. */
    val level: Int?,
    /** The band's name in the reader's language, e.g. "优" or "Excellent". */
    val category: String?,
    /** The pollutant that drives the index, e.g. "PM10"; may be "-" when nothing dominates. */
    val primary: String?,
    val pollutants: AirPollutants?,
) {
    /** True when [primary] names a real pollutant rather than the placeholder the service sends. */
    val hasPrimary: Boolean
        get() = !primary.isNullOrBlank() && primary != "-" && !primary.equals("NA", ignoreCase = true)
}

/** Concentrations in µg/m³, except [co] which the service reports in mg/m³. */
data class AirPollutants(
    val pm25: Double?,
    val pm10: Double?,
    val o3: Double?,
    val no2: Double?,
    val so2: Double?,
    val co: Double?,
)