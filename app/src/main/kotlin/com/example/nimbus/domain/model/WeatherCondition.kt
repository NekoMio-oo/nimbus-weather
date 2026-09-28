package com.example.nimbus.domain.model

/**
 * The sky, reduced to what the UI can draw. Open-Meteo reports WMO weather interpretation codes; the
 * grouping here folds intensities together (light, moderate and heavy rain are all [RAIN]) because the
 * icon and the sky colour are the same for each, and the temperature and precipitation chance already
 * tell the user how bad it is.
 */
enum class WeatherCondition {
    CLEAR,
    MAINLY_CLEAR,
    PARTLY_CLOUDY,
    OVERCAST,
    FOG,
    DRIZZLE,
    RAIN,
    FREEZING_RAIN,
    SNOW,
    SHOWERS,
    THUNDERSTORM;

    /** True for conditions where rain or snow is actually falling. */
    val isPrecipitating: Boolean
        get() = this == DRIZZLE || this == RAIN || this == FREEZING_RAIN || this == SNOW ||
            this == SHOWERS || this == THUNDERSTORM

    companion object {
        /** Map a WMO 4677 weather code (as used by Open-Meteo) onto a condition. Unknown codes read as overcast. */
        fun fromWmoCode(code: Int): WeatherCondition = when (code) {
            0 -> CLEAR
            1 -> MAINLY_CLEAR
            2 -> PARTLY_CLOUDY
            3 -> OVERCAST
            45, 48 -> FOG
            51, 53, 55 -> DRIZZLE
            56, 57, 66, 67 -> FREEZING_RAIN
            61, 63, 65 -> RAIN
            71, 73, 75, 77, 85, 86 -> SNOW
            80, 81, 82 -> SHOWERS
            95, 96, 99 -> THUNDERSTORM
            else -> OVERCAST
        }
    }
}
