package com.example.nimbus.domain.model

/** Which family of units the user reads: metric (°C, km/h, mm, hPa) or imperial (°F, mph, in, inHg). */
enum class UnitSystem {
    METRIC,
    IMPERIAL;

    fun toggled(): UnitSystem = if (this == METRIC) IMPERIAL else METRIC
}
