package com.example.nimbus.util

import com.example.nimbus.domain.model.Place
import com.example.nimbus.domain.model.UnitSystem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** Numbers and times as the screen shows them. Every value arrives metric and is converted here, once. */
object Formatters {

    fun celsiusIn(unitSystem: UnitSystem, celsius: Double): Double =
        if (unitSystem == UnitSystem.METRIC) celsius else celsius * 9.0 / 5.0 + 32.0

    /** "31°": the degree sign without a scale letter, for places where the scale is obvious. */
    fun temperature(celsius: Double, unitSystem: UnitSystem): String =
        "${celsiusIn(unitSystem, celsius).roundToInt()}°"

    fun speed(kmh: Double, unitSystem: UnitSystem): String =
        if (unitSystem == UnitSystem.METRIC) "${kmh.roundToInt()} km/h"
        else "${(kmh * KM_TO_MILES).roundToInt()} mph"

    fun precipitation(mm: Double, unitSystem: UnitSystem): String =
        if (unitSystem == UnitSystem.METRIC) "${oneDecimal(mm)} mm"
        else "${twoDecimals(mm / MM_PER_INCH)} in"

    fun pressure(hpa: Double, unitSystem: UnitSystem): String =
        if (unitSystem == UnitSystem.METRIC) "${hpa.roundToInt()} hPa"
        else "${oneDecimal(hpa * HPA_TO_INHG)} inHg"

    fun uvIndex(uv: Double): String = uv.roundToInt().toString()

    /** "1 PM", in the device locale's clock convention. */
    fun hour(time: LocalDateTime): String = time.format(hourFormatter)

    /** "5:47 AM". */
    fun clock(time: LocalTime): String = time.format(clockFormatter)

    /** "Mon". */
    fun weekday(date: LocalDate): String = date.format(weekdayFormatter)

    /** A compass point for a meteorological wind direction in degrees. */
    fun compass(degrees: Int): String {
        val index = (((degrees % 360 + 360) % 360 + 22.5) / 45.0).toInt() % COMPASS_POINTS.size
        return COMPASS_POINTS[index]
    }

    private fun oneDecimal(value: Double): String = String.format(Locale.getDefault(), "%.1f", value)

    private fun twoDecimals(value: Double): String = String.format(Locale.getDefault(), "%.2f", value)

    private val hourFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h a", Locale.getDefault())
    private val clockFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    private val weekdayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())

    private val COMPASS_POINTS = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    private const val KM_TO_MILES = 0.621371
    private const val MM_PER_INCH = 25.4
    private const val HPA_TO_INHG = 0.02953
}

/** The place's zone, or the device zone when the geocoder gave one Java does not know. */
fun Place.zoneId(): ZoneId = runCatching { ZoneId.of(timeZone) }.getOrDefault(ZoneId.systemDefault())
