package com.example.nimbus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.example.nimbus.R
import com.example.nimbus.domain.model.ForecastException
import com.example.nimbus.domain.model.WeatherCondition

/** The condition as a sentence fragment, e.g. "Partly cloudy" or "Clear night". */
@Composable
fun conditionLabel(condition: WeatherCondition, isDay: Boolean): String = stringResource(
    when (condition) {
        WeatherCondition.CLEAR -> if (isDay) R.string.condition_clear else R.string.condition_clear_night
        WeatherCondition.MAINLY_CLEAR -> R.string.condition_mainly_clear
        WeatherCondition.PARTLY_CLOUDY -> R.string.condition_partly_cloudy
        WeatherCondition.OVERCAST -> R.string.condition_overcast
        WeatherCondition.FOG -> R.string.condition_fog
        WeatherCondition.DRIZZLE -> R.string.condition_drizzle
        WeatherCondition.RAIN -> R.string.condition_rain
        WeatherCondition.FREEZING_RAIN -> R.string.condition_freezing_rain
        WeatherCondition.SNOW -> R.string.condition_snow
        WeatherCondition.SHOWERS -> R.string.condition_showers
        WeatherCondition.THUNDERSTORM -> R.string.condition_thunderstorm
    },
)

/** The WHO-style UV index bands. */
@Composable
fun uvLabel(uv: Double): String = stringResource(
    when {
        uv < 3 -> R.string.uv_low
        uv < 6 -> R.string.uv_moderate
        uv < 8 -> R.string.uv_high
        uv < 11 -> R.string.uv_very_high
        else -> R.string.uv_extreme
    },
)

/**
 * The compass point a meteorological direction (the direction the wind blows *from*) describes, in the
 * reader's language. The eight points are a resource array so they can be translated with the rest of the UI.
 */
@Composable
fun compassLabel(degrees: Int): String {
    val points = stringArrayResource(R.array.compass_points)
    val normalised = (degrees % 360 + 360) % 360
    return points[((normalised + 22.5) / 45.0).toInt() % points.size]
}

@Composable
fun ForecastException.title(): String = stringResource(
    when (this) {
        is ForecastException.Offline -> R.string.error_offline_title
        is ForecastException.Timeout -> R.string.error_timeout_title
        is ForecastException.Server -> R.string.error_server_title
        is ForecastException.Unexpected -> R.string.error_unknown_title
    },
)

@Composable
fun ForecastException.body(): String = when (this) {
    is ForecastException.Offline -> stringResource(R.string.error_offline_body)
    is ForecastException.Timeout -> stringResource(R.string.error_timeout_body)
    is ForecastException.Server -> stringResource(R.string.error_server_body, statusCode)
    is ForecastException.Unexpected -> stringResource(R.string.error_unknown_body)
}
