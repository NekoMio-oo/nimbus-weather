package com.example.nimbus.ui.preview

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
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime

/** Fixed forecasts for @Preview composables, shaped like a real uapis.cn answer so the layout is honest. */
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
    private val hourlyConditions = listOf(
        WeatherCondition.DRIZZLE, WeatherCondition.THUNDERSTORM, WeatherCondition.THUNDERSTORM,
        WeatherCondition.DRIZZLE, WeatherCondition.DRIZZLE, WeatherCondition.DRIZZLE,
        WeatherCondition.OVERCAST, WeatherCondition.OVERCAST, WeatherCondition.PARTLY_CLOUDY,
        WeatherCondition.PARTLY_CLOUDY, WeatherCondition.MAINLY_CLEAR, WeatherCondition.CLEAR,
        WeatherCondition.CLEAR, WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR,
        WeatherCondition.PARTLY_CLOUDY, WeatherCondition.OVERCAST, WeatherCondition.OVERCAST,
        WeatherCondition.RAIN, WeatherCondition.RAIN, WeatherCondition.SHOWERS,
        WeatherCondition.SHOWERS, WeatherCondition.PARTLY_CLOUDY, WeatherCondition.MAINLY_CLEAR,
    )
    private val hourlyChances = listOf(40, 75, 80, 60, 45, 35, 20, 15, 10, 5, 0, 0, 0, 0, 5, 10, 20, 25, 55, 60, 70, 65, 30, 15)

    private val dailyConditions = listOf(
        WeatherCondition.THUNDERSTORM, WeatherCondition.SHOWERS, WeatherCondition.OVERCAST,
        WeatherCondition.PARTLY_CLOUDY, WeatherCondition.RAIN, WeatherCondition.CLEAR,
        WeatherCondition.MAINLY_CLEAR,
    )
    private val dailyMax = listOf(31.2, 30.4, 32.1, 32.8, 29.6, 33.0, 32.4)
    private val dailyMin = listOf(24.5, 24.9, 25.1, 25.6, 24.2, 25.8, 25.3)
    private val dailyChances = listOf(80, 70, 20, 10, 65, 0, 15)
    private val dailyUv = listOf(6.5, 7.1, 9.0, 10.2, 5.4, 11.0, 9.6)

    /** A minute-level radar strip: a shower that builds, peaks, then eases off over the next couple of hours. */
    private val minutelyIntensities = listOf(
        0.0, 0.0, 0.1, 0.2, 0.4, 0.6, 0.9, 1.3, 1.8, 2.4, 3.1, 3.6, 3.9, 4.2, 4.0, 3.6, 3.1, 2.6, 2.1, 1.7,
        1.4, 1.1, 0.9, 0.7, 0.6, 0.5, 0.4, 0.3, 0.3, 0.2, 0.2, 0.1, 0.1, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
    )

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
            visibilityKm = 16.0,
            cloudPercent = 55,
        ),
        hourly = hourlyTemperatures.indices.map { i ->
            HourlyForecast(
                time = start.plusHours(i.toLong()),
                temperatureC = hourlyTemperatures[i],
                condition = hourlyConditions[i],
                precipitationChance = hourlyChances[i],
            )
        },
        daily = dailyConditions.indices.map { d ->
            DailyForecast(
                date = start.toLocalDate().plusDays(d.toLong()),
                condition = dailyConditions[d],
                maxTemperatureC = dailyMax[d],
                minTemperatureC = dailyMin[d],
                sunrise = LocalTime.of(5, 47),
                sunset = LocalTime.of(18, 2),
                precipitationChance = dailyChances[d],
                uvIndexMax = dailyUv[d],
            )
        },
        minutely = MinutelyPrecipitation(
            summary = "20 分钟左右雨渐停，不过一个半小时后还会下雨",
            updateTime = start,
            points = minutelyIntensities.mapIndexed { i, mmPerHour ->
                PrecipitationPoint(
                    time = start.plusMinutes(i * 3L),
                    mmPerHour = mmPerHour,
                    type = "rain",
                )
            },
        ),
        airQuality = AirQuality(
            aqi = 68,
            level = 2,
            category = "良",
            primary = "PM10",
            pollutants = AirPollutants(pm25 = 42.0, pm10 = 68.0, o3 = 88.0, no2 = 15.0, so2 = 6.0, co = 0.7),
        ),
        lifeIndices = listOf(
            LifeIndex(LifeIndexType.CLOTHING, "炎热", "短袖"),
            LifeIndex(LifeIndexType.COMFORT, "较不舒适", "闷热"),
            LifeIndex(LifeIndexType.UMBRELLA, "需要", "有雨"),
            LifeIndex(LifeIndexType.UV, "强", "涂防晒"),
            LifeIndex(LifeIndexType.EXERCISE, "较不宜", "有雨"),
            LifeIndex(LifeIndexType.CAR_WASH, "不宜", "有雨"),
            LifeIndex(LifeIndexType.COLD_RISK, "少发", "无明显降温"),
            LifeIndex(LifeIndexType.ALLERGY, "较易发", "注意防护"),
            LifeIndex(LifeIndexType.DRYING, "不宜", "有雨"),
        ),
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