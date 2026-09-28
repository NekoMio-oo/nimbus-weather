package com.example.nimbus.domain.model

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** The weather right now at a place. Temperatures are Celsius, speeds km/h; the UI converts for display. */
data class CurrentConditions(
    val time: LocalDateTime,
    val temperatureC: Double,
    val apparentTemperatureC: Double,
    val humidityPercent: Int,
    val precipitationMm: Double,
    val condition: WeatherCondition,
    val isDay: Boolean,
    val windSpeedKmh: Double,
    val windDirectionDegrees: Int,
    val pressureHpa: Double,
    val uvIndex: Double,
    /** Horizontal visibility in kilometres, when the service reports it. */
    val visibilityKm: Double?,
    /** Share of the sky under cloud, 0–100, when the service reports it. */
    val cloudPercent: Int?,
)

/** One hour of the forecast, in the place's local time. */
data class HourlyForecast(
    val time: LocalDateTime,
    val temperatureC: Double,
    val condition: WeatherCondition,
    val precipitationChance: Int,
)

/** One day of the forecast, in the place's local time. */
data class DailyForecast(
    val date: LocalDate,
    val condition: WeatherCondition,
    val maxTemperatureC: Double,
    val minTemperatureC: Double,
    val sunrise: LocalTime,
    val sunset: LocalTime,
    val precipitationChance: Int,
    val uvIndexMax: Double,
) {
    val daylight: Duration get() = Duration.between(sunrise, sunset)
}

/** Everything the home screen shows for one place, plus when it was fetched so staleness can be judged. */
data class Forecast(
    val place: Place,
    val current: CurrentConditions,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    /** Minute-level precipitation, absent for places the service does not cover. */
    val minutely: MinutelyPrecipitation?,
    /** The air quality index and pollutant concentrations. */
    val airQuality: AirQuality?,
    /** The daily life indices (dressing, UV, car washing, …). */
    val lifeIndices: List<LifeIndex>,
    val fetchedAt: Instant,
) {
    val today: DailyForecast? get() = daily.firstOrNull()

    /** Whether [time] falls between that day's sunrise and sunset; falls back to a 6–18 window. */
    fun isDaylight(time: LocalDateTime): Boolean {
        val day = daily.firstOrNull { it.date == time.toLocalDate() }
            ?: return time.hour in 6..18
        return !time.toLocalTime().isBefore(day.sunrise) && time.toLocalTime().isBefore(day.sunset)
    }

    /** True while the forecast is younger than [maxAge]. */
    fun isFresh(now: Instant, maxAge: Duration): Boolean = Duration.between(fetchedAt, now) < maxAge
}
