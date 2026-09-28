package com.example.nimbus.data.repository

import com.example.nimbus.data.local.StoredPlace
import com.example.nimbus.data.remote.AirPollutantsDto
import com.example.nimbus.data.remote.DailyForecastDto
import com.example.nimbus.data.remote.GeocodingResult
import com.example.nimbus.data.remote.HourlyForecastDto
import com.example.nimbus.data.remote.LifeIndexDto
import com.example.nimbus.data.remote.MinutelyDto
import com.example.nimbus.data.remote.WeatherResponse
import com.example.nimbus.domain.model.AirPollutants
import com.example.nimbus.domain.model.AirQuality
import com.example.nimbus.domain.model.CurrentConditions
import com.example.nimbus.domain.model.DailyForecast
import com.example.nimbus.domain.model.Forecast
import com.example.nimbus.domain.model.HourlyForecast
import com.example.nimbus.domain.model.LifeIndex
import com.example.nimbus.domain.model.LifeIndexType
import com.example.nimbus.domain.model.MinutelyPrecipitation
import com.example.nimbus.domain.model.Place
import com.example.nimbus.domain.model.PrecipitationPoint
import com.example.nimbus.domain.model.WeatherCondition
import com.example.nimbus.util.zoneId
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// Wire and disk shapes → domain. The uapis.cn answer is a bag of optional blocks, so each mapper returns
// null (or an empty list) for anything the service left out rather than failing the whole forecast.

internal fun GeocodingResult.toDomain(): Place = Place(
    id = id,
    name = name,
    region = admin1,
    country = country ?: countryCode ?: "",
    latitude = latitude,
    longitude = longitude,
    timeZone = timezone ?: "UTC",
)

internal fun StoredPlace.toDomain(): Place =
    Place(id, name, region, country, latitude, longitude, timeZone)

internal fun Place.toStored(): StoredPlace =
    StoredPlace(id, name, region, country, latitude, longitude, timeZone)

internal fun WeatherResponse.toDomain(place: Place, fetchedAt: Instant): Forecast {
    val days = forecast.orEmpty().mapNotNull { it.toDomain() }
    val hours = hourlyForecast.orEmpty().mapNotNull { it.toDomain() }
    val today = days.firstOrNull()

    // The service's `report_time` is either an absolute stamp or a relative sentence ("11 分钟前发布"),
    // so it is used only when it parses; otherwise the minute-level update time or the first hour stands in.
    val now = parseDateTime(reportTime)
        ?: parseOffsetDateTime(minutely()?.updateTime)
        ?: hours.firstOrNull()?.time
        ?: ZonedDateTime.now(place.zoneId()).toLocalDateTime()

    val firstHour = hourlyForecast?.firstOrNull()
    return Forecast(
        place = place,
        current = CurrentConditions(
            time = now,
            temperatureC = temperature,
            apparentTemperatureC = feelsLike ?: temperature,
            humidityPercent = humidity,
            precipitationMm = precipitation ?: 0.0,
            condition = currentCondition(weatherIcon, weather),
            isDay = isDaylight(now, today) ?: !WeatherCondition.isNightIconCode(weatherIcon),
            windSpeedKmh = firstHour?.windSpeed ?: parseWindPowerKmh(windPower),
            windDirectionDegrees = parseDirection(windDirection ?: firstHour?.windDirection),
            pressureHpa = pressure ?: STANDARD_PRESSURE_HPA,
            uvIndex = uv ?: 0.0,
            visibilityKm = visibility,
            cloudPercent = cloud,
        ),
        hourly = hours,
        daily = days,
        minutely = minutely()?.toDomain(),
        airQuality = toAirQuality(),
        lifeIndices = toLifeIndices(),
        fetchedAt = fetchedAt,
    )
}

/** The service sends both `minutely_precip` and `minutely_forecast`, with the same content; take either. */
private fun WeatherResponse.minutely(): MinutelyDto? = minutelyPrecip ?: minutelyForecast

/**
 * The current sky, preferring the icon code (a stable QWeather-style number) and falling back to the text.
 * The 1000-series codes are disaster warnings that carry no sky, so those read the text instead, and the
 * `else` branch covers an answer with neither.
 */
private fun currentCondition(icon: String?, text: String): WeatherCondition {
    val code = icon?.toIntOrNull()
    return when {
        code != null && code < 1000 -> WeatherCondition.fromIconCode(icon)
        text.isNotBlank() -> WeatherCondition.fromText(text)
        code != null -> WeatherCondition.fromIconCode(icon)
        else -> WeatherCondition.OVERCAST
    }
}

/** Whether [time] falls between that day's sunrise and sunset, or null when the day's times are unknown. */
private fun isDaylight(time: LocalDateTime, day: DailyForecast?): Boolean? {
    if (day == null) return null
    return !time.toLocalTime().isBefore(day.sunrise) && time.toLocalTime().isBefore(day.sunset)
}

private fun HourlyForecastDto.toDomain(): HourlyForecast? {
    val parsed = parseDateTime(time) ?: return null
    return HourlyForecast(
        time = parsed,
        temperatureC = temperature,
        condition = WeatherCondition.fromText(weather),
        precipitationChance = pop ?: 0,
    )
}

private fun DailyForecastDto.toDomain(): DailyForecast? {
    val parsed = runCatching { LocalDate.parse(date) }.getOrNull() ?: return null
    return DailyForecast(
        date = parsed,
        condition = WeatherCondition.fromText(weatherDay ?: weatherNight),
        maxTemperatureC = tempMax,
        minTemperatureC = tempMin,
        sunrise = parseTime(sunrise) ?: DEFAULT_SUNRISE,
        sunset = parseTime(sunset) ?: DEFAULT_SUNSET,
        precipitationChance = pop ?: 0,
        uvIndexMax = uvIndex ?: 0.0,
    )
}

private fun MinutelyDto.toDomain(): MinutelyPrecipitation? {
    val parsed = data.mapNotNull { point ->
        val time = parseOffsetDateTime(point.time) ?: return@mapNotNull null
        PrecipitationPoint(time = time, mmPerHour = point.precip, type = point.type ?: "rain")
    }
    if (parsed.isEmpty()) return null
    return MinutelyPrecipitation(
        summary = summary?.takeIf { it.isNotBlank() },
        updateTime = parseOffsetDateTime(updateTime),
        points = parsed,
    )
}

private fun WeatherResponse.toAirQuality(): AirQuality? {
    val index = aqi ?: return null
    return AirQuality(
        aqi = index,
        level = aqiLevel,
        category = aqiCategory?.takeIf { it.isNotBlank() },
        primary = aqiPrimary,
        pollutants = airPollutants?.toDomain(),
    )
}

private fun AirPollutantsDto.toDomain(): AirPollutants =
    AirPollutants(pm25 = pm25, pm10 = pm10, o3 = o3, no2 = no2, so2 = so2, co = co)

/** Ordered by [LifeIndexType], so the screen shows the everyday indices before the niche ones. */
private fun WeatherResponse.toLifeIndices(): List<LifeIndex> {
    val source = lifeIndices ?: return emptyList()
    return LifeIndexType.entries.mapNotNull { type ->
        val dto: LifeIndexDto = source[type.apiKey] ?: return@mapNotNull null
        LifeIndex(
            type = type,
            level = dto.level?.takeIf { it.isNotBlank() } ?: dto.brief.orEmpty(),
            brief = dto.brief?.takeIf { it.isNotBlank() }.orEmpty(),
        )
    }
}

/** "2026-09-28 10:54:13", "2026-09-28 12:00" or an ISO stamp; null for a relative sentence. */
private fun parseDateTime(raw: String?): LocalDateTime? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    return runCatching { LocalDateTime.parse(value, DATE_TIME_SECONDS) }.getOrNull()
        ?: runCatching { LocalDateTime.parse(value, DATE_TIME_MINUTES) }.getOrNull()
        ?: runCatching { LocalDateTime.parse(value) }.getOrNull()
}

/** The minute-level radar stamps carry an offset, e.g. "2026-09-28T11:13:06+08:00". */
private fun parseOffsetDateTime(raw: String?): LocalDateTime? =
    raw?.let { runCatching { OffsetDateTime.parse(it).toLocalDateTime() }.getOrNull() }

private fun parseTime(raw: String?): LocalTime? =
    raw?.let { runCatching { LocalTime.parse(it, TIME_HH_MM) }.getOrNull() }

/**
 * The compass bearing of a direction text, in degrees. Accepts the Chinese forms ("东北风") and the English
 * abbreviations ("NE"); the two-character points are tested before the single ones so 东北 never reads as 北.
 * Calm or an unknown word is reported as due north, which is what the compass labels call "N".
 */
internal fun parseDirection(text: String?): Int {
    val value = text?.trim()?.uppercase().orEmpty().removeSuffix("风")
    return when {
        value.isEmpty() -> 0
        "东北" in value || "NE" in value -> 45
        "东南" in value || "SE" in value -> 135
        "西南" in value || "SW" in value -> 225
        "西北" in value || "NW" in value -> 315
        "北" in value || "N" in value -> 0
        "东" in value || "E" in value -> 90
        "南" in value || "S" in value -> 180
        "西" in value || "W" in value -> 270
        else -> 0
    }
}

/**
 * A Beaufort level such as "3级" or "Level 3", as the mid-point of that level in km/h. Used only when the
 * hourly series carries no wind speed of its own.
 */
internal fun parseWindPowerKmh(power: String?): Double {
    val level = Regex("(\\d+)").find(power.orEmpty())?.groupValues?.get(1)?.toIntOrNull() ?: return 0.0
    return BEAUFORT_KMH.getOrElse(level) { BEAUFORT_KMH.last() }
}

private val DATE_TIME_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private val DATE_TIME_MINUTES = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
private val TIME_HH_MM = DateTimeFormatter.ofPattern("HH:mm")

/** The mid-point of each Beaufort level, 0–12, in km/h. */
private val BEAUFORT_KMH = listOf(0.5, 5.0, 12.0, 20.0, 29.0, 39.0, 50.0, 62.0, 75.0, 89.0, 103.0, 118.0, 130.0)

private val DEFAULT_SUNRISE = LocalTime.of(6, 0)
private val DEFAULT_SUNSET = LocalTime.of(18, 0)
private const val STANDARD_PRESSURE_HPA = 1013.0