package com.example.nimbus.ui.preview

import com.example.nimbus.domain.model.CurrentConditions
import com.example.nimbus.domain.model.DailyForecast
import com.example.nimbus.domain.model.Forecast
import com.example.nimbus.domain.model.HourlyForecast
import com.example.nimbus.domain.model.Place
import com.example.nimbus.domain.model.WeatherCondition
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime

/** Fixed forecasts for @Preview composables, taken from a real Open-Meteo answer so the shapes are honest. */
object PreviewData {

    val manila = Place(
        id = 1701668,
        name = "Manila",
        region = "National Capital Region",
        country = "Philippines",
        latitude = 14.6042,
        longitude = 120.9822,
        timeZone = "Asia/Manila",
    )

    val reykjavik = Place(
        id = 3413829,
        name = "Reykjavík",
        region = "Capital Region",
        country = "Iceland",
        latitude = 64.1355,
        longitude = -21.8954,
        timeZone = "Atlantic/Reykjavik",
    )

    val places: List<Place> = listOf(manila, reykjavik)

    private val start: LocalDateTime = LocalDateTime.of(2026, 9, 13, 13, 0)

    private val hourlyTemperatures = listOf(
        30.1, 28.8, 26.5, 26.3, 26.6, 25.4, 25.3, 25.3, 25.5, 25.5, 25.3, 25.0,
        25.1, 25.1, 24.6, 24.4, 24.3, 24.2, 25.0, 26.3, 27.7, 29.3, 29.8, 29.3,
    )
    private val hourlyCodes = listOf(51, 95, 96, 55, 51, 53, 3, 3, 2, 2, 1, 0, 0, 0, 1, 2, 3, 3, 61, 61, 80, 80, 2, 1)
    private val hourlyChances = listOf(40, 75, 80, 60, 45, 35, 20, 15, 10, 5, 0, 0, 0, 0, 5, 10, 20, 25, 55, 60, 70, 65, 30, 15)

    private val dailyCodes = listOf(95, 80, 3, 2, 61, 0, 1)
    private val dailyMax = listOf(31.2, 30.4, 32.1, 32.8, 29.6, 33.0, 32.4)
    private val dailyMin = listOf(24.5, 24.9, 25.1, 25.6, 24.2, 25.8, 25.3)
    private val dailyChances = listOf(80, 70, 20, 10, 65, 0, 15)
    private val dailyUv = listOf(6.5, 7.1, 9.0, 10.2, 5.4, 11.0, 9.6)

    val forecast: Forecast = Forecast(
        place = manila,
        current = CurrentConditions(
            time = start,
            temperatureC = 30.1,
            apparentTemperatureC = 36.9,
            humidityPercent = 74,
            precipitationMm = 0.1,
            condition = WeatherCondition.PARTLY_CLOUDY,
            isDay = true,
            windSpeedKmh = 10.5,
            windDirectionDegrees = 273,
            pressureHpa = 1008.8,
            uvIndex = 7.8,
        ),
        hourly = hourlyTemperatures.indices.map { i ->
            HourlyForecast(
                time = start.plusHours(i.toLong()),
                temperatureC = hourlyTemperatures[i],
                condition = WeatherCondition.fromWmoCode(hourlyCodes[i]),
                precipitationChance = hourlyChances[i],
            )
        },
        daily = dailyCodes.indices.map { d ->
            DailyForecast(
                date = start.toLocalDate().plusDays(d.toLong()),
                condition = WeatherCondition.fromWmoCode(dailyCodes[d]),
                maxTemperatureC = dailyMax[d],
                minTemperatureC = dailyMin[d],
                sunrise = LocalTime.of(5, 47),
                sunset = LocalTime.of(18, 2),
                precipitationChance = dailyChances[d],
                uvIndexMax = dailyUv[d],
            )
        },
        fetchedAt = Instant.parse("2026-09-13T05:00:00Z"),
    )

    /** A cold, dark counterpart so previews can show the night palette and the snow icon. */
    val snowyNight: Forecast = forecast.copy(
        place = reykjavik,
        current = forecast.current.copy(
            temperatureC = -3.0,
            apparentTemperatureC = -8.5,
            humidityPercent = 88,
            condition = WeatherCondition.SNOW,
            isDay = false,
            windSpeedKmh = 32.0,
            uvIndex = 0.0,
        ),
    )
}
