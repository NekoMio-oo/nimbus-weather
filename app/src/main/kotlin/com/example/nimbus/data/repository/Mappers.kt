package com.example.nimbus.data.repository

import com.example.nimbus.data.local.StoredPlace
import com.example.nimbus.data.remote.DailyDto
import com.example.nimbus.data.remote.ForecastResponse
import com.example.nimbus.data.remote.GeocodingResult
import com.example.nimbus.data.remote.HourlyDto
import com.example.nimbus.domain.model.CurrentConditions
import com.example.nimbus.domain.model.DailyForecast
import com.example.nimbus.domain.model.Forecast
import com.example.nimbus.domain.model.HourlyForecast
import com.example.nimbus.domain.model.Place
import com.example.nimbus.domain.model.WeatherCondition
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

// Wire and disk shapes → domain. Each mapper tolerates the gaps Open-Meteo can report (a null in a series)
// by dropping that entry rather than failing the whole forecast.

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

internal fun ForecastResponse.toDomain(place: Place, fetchedAt: Instant): Forecast = Forecast(
    place = place,
    current = CurrentConditions(
        time = LocalDateTime.parse(current.time),
        temperatureC = current.temperature,
        apparentTemperatureC = current.apparentTemperature,
        humidityPercent = current.humidity,
        precipitationMm = current.precipitation,
        condition = WeatherCondition.fromWmoCode(current.weatherCode),
        isDay = current.isDay == 1,
        windSpeedKmh = current.windSpeed,
        windDirectionDegrees = current.windDirection,
        pressureHpa = current.surfacePressure,
        uvIndex = current.uvIndex ?: 0.0,
    ),
    hourly = hourly.toDomain(),
    daily = daily.toDomain(),
    fetchedAt = fetchedAt,
)

private fun HourlyDto.toDomain(): List<HourlyForecast> = time.indices.mapNotNull { i ->
    val temperature = temperature.getOrNull(i) ?: return@mapNotNull null
    HourlyForecast(
        time = LocalDateTime.parse(time[i]),
        temperatureC = temperature,
        condition = WeatherCondition.fromWmoCode(weatherCode.getOrNull(i) ?: 3),
        precipitationChance = precipitationProbability.getOrNull(i) ?: 0,
    )
}

private fun DailyDto.toDomain(): List<DailyForecast> = time.indices.mapNotNull { i ->
    val max = temperatureMax.getOrNull(i) ?: return@mapNotNull null
    val min = temperatureMin.getOrNull(i) ?: return@mapNotNull null
    val sunrise = sunrise.getOrNull(i) ?: return@mapNotNull null
    val sunset = sunset.getOrNull(i) ?: return@mapNotNull null
    DailyForecast(
        date = LocalDate.parse(time[i]),
        condition = WeatherCondition.fromWmoCode(weatherCode.getOrNull(i) ?: 3),
        maxTemperatureC = max,
        minTemperatureC = min,
        sunrise = LocalDateTime.parse(sunrise).toLocalTime(),
        sunset = LocalDateTime.parse(sunset).toLocalTime(),
        precipitationChance = precipitationProbabilityMax.getOrNull(i) ?: 0,
        uvIndexMax = uvIndexMax.getOrNull(i) ?: 0.0,
    )
}
