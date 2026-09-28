package com.example.nimbus.domain.model

/**
 * The sky, reduced to what the UI can draw. UApiPro reports a QWeather-style icon code for the current
 * conditions and only a description for the forecast series, so both are folded into this set here; the
 * grouping merges intensities together (light, moderate and heavy rain are all [RAIN]) because the icon
 * and the sky colour are the same for each, and the temperature and precipitation chance already tell the
 * user how bad it is.
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
        /**
         * Map a QWeather icon code (the `weather_icon` field) onto a condition. Codes 100-515 are the
         * ordinary sky; the 150s, 350s and 456/457 are their night variants and the 800s the moon phases.
         * The 1000s are disaster warnings, which carry no sky information, so they fall back to the text
         * description instead. Unknown codes read as overcast.
         */
        fun fromIconCode(code: String?): WeatherCondition = when (code?.toIntOrNull()) {
            100, 150, 900, 901 -> CLEAR
            102, 103, 152, 153 -> MAINLY_CLEAR
            101, 151 -> PARTLY_CLOUDY
            104 -> OVERCAST
            300, 301, 350, 351, 399 -> SHOWERS
            302, 303, 304 -> THUNDERSTORM
            309 -> DRIZZLE
            313 -> FREEZING_RAIN
            305, 306, 307, 308, 310, 311, 312, 314, 315, 316, 317, 318 -> RAIN
            400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 456, 457, 499 -> SNOW
            500, 501, 502, 503, 504, 507, 508, 509, 510, 511, 512, 513, 514, 515 -> FOG
            in 800..807 -> CLEAR
            else -> OVERCAST
        }

        /**
         * Map a description such as "中到大雨" or "Patchy rain nearby" onto a condition. Used where the
         * service only describes the weather in words — the hourly and daily series — and as the fallback
         * for warning codes. Order matters: 冻雨 must read as freezing rain before 雨 is considered.
         */
        fun fromText(text: String?): WeatherCondition {
            val value = text?.lowercase().orEmpty()
            return when {
                THUNDER_WORDS.any { value.contains(it) } -> THUNDERSTORM
                SNOW_WORDS.any { value.contains(it) } -> SNOW
                FREEZING_WORDS.any { value.contains(it) } -> FREEZING_RAIN
                DRIZZLE_WORDS.any { value.contains(it) } -> DRIZZLE
                SHOWER_WORDS.any { value.contains(it) } -> SHOWERS
                RAIN_WORDS.any { value.contains(it) } -> RAIN
                FOG_WORDS.any { value.contains(it) } -> FOG
                OVERCAST_WORDS.any { value.contains(it) } -> OVERCAST
                CLOUD_WORDS.any { value.contains(it) } -> PARTLY_CLOUDY
                CLEAR_WORDS.any { value.contains(it) } -> CLEAR
                else -> OVERCAST
            }
        }

        /** True for the icon codes the service reserves for night, which is all the sky tells us there. */
        fun isNightIconCode(code: String?): Boolean = when (code?.toIntOrNull()) {
            150, 151, 152, 153, 350, 351, 456, 457 -> true
            in 800..807 -> true
            else -> false
        }

        private val THUNDER_WORDS = listOf("雷", "thunder")
        private val SNOW_WORDS = listOf("雪", "snow", "sleet", "blizzard")
        private val FREEZING_WORDS = listOf("冻雨", "freezing")
        private val DRIZZLE_WORDS = listOf("毛毛雨", "细雨", "drizzle")
        private val SHOWER_WORDS = listOf("阵雨", "shower")
        private val RAIN_WORDS = listOf("雨", "rain")
        private val FOG_WORDS = listOf("雾", "霾", "浮尘", "扬沙", "沙尘", "fog", "mist", "haze", "dust", "sand")
        private val OVERCAST_WORDS = listOf("阴", "overcast")
        private val CLOUD_WORDS = listOf("云", "cloud")
        private val CLEAR_WORDS = listOf("晴", "clear", "sunny")
    }
}