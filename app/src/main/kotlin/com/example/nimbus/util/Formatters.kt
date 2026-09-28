package com.example.nimbus.util

import android.content.Context
import android.text.format.DateFormat
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

    // Declared before the formatters below: object properties initialise top to bottom, and the formatter
    // builders read this pattern.
    private val MINUTE_FIELD = Regex("[:：]?\\s?m{1,2}\\s?")
    private const val KM_TO_MILES = 0.621371
    private const val MM_PER_INCH = 25.4
    private const val HPA_TO_INHG = 0.02953

    // Time patterns are built from the app's locale and the device's 12/24-hour setting. Both can change
    // while the process lives (a per-app language switch, a system time-format change), and building them
    // once at class load would freeze the very first locale it saw, so [configure] rebuilds them.
    private var locale: Locale = Locale.getDefault()
    private var hourFormatter: DateTimeFormatter = buildHourFormatter(locale, use24Hour = false)
    private var clockFormatter: DateTimeFormatter = buildClockFormatter(locale, use24Hour = false)
    private var weekdayFormatter: DateTimeFormatter = buildWeekdayFormatter(locale)

    /**
     * Adopt the app's language and the device's clock convention. Called from the Activity before the first
     * composition, and again whenever the Activity is recreated after a locale or setting change.
     */
    fun configure(context: Context) {
        val configuration = context.resources.configuration
        locale = configuration.locales[0]
        val use24Hour = DateFormat.is24HourFormat(context)
        hourFormatter = buildHourFormatter(locale, use24Hour)
        clockFormatter = buildClockFormatter(locale, use24Hour)
        weekdayFormatter = buildWeekdayFormatter(locale)
    }

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

    /** The hour of [time], in the app locale's clock convention, e.g. "1 PM" or "13". */
    fun hour(time: LocalDateTime): String = time.format(hourFormatter)

    /** The time of day, e.g. "5:47 AM" or "05:47". */
    fun clock(time: LocalTime): String = time.format(clockFormatter)

    /** "Mon" / "周一". */
    fun weekday(date: LocalDate): String = date.format(weekdayFormatter)

    private fun oneDecimal(value: Double): String = String.format(locale, "%.1f", value)

    private fun twoDecimals(value: Double): String = String.format(locale, "%.2f", value)

    private fun buildHourFormatter(locale: Locale, use24Hour: Boolean): DateTimeFormatter =
        DateTimeFormatter.ofPattern(timePattern(locale, use24Hour, withMinutes = false), locale)

    private fun buildClockFormatter(locale: Locale, use24Hour: Boolean): DateTimeFormatter =
        DateTimeFormatter.ofPattern(timePattern(locale, use24Hour, withMinutes = true), locale)

    private fun buildWeekdayFormatter(locale: Locale): DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE", locale)

    /**
     * The best time pattern for [locale], via the platform's CLDR data. The skeleton decides the fields;
     * the locale decides their order and its day-period marker, which is why a Chinese 12-hour clock reads
     * "上午8" and not the "8 上午" a hand-written "h a" pattern would produce.
     */
    private fun timePattern(locale: Locale, use24Hour: Boolean, withMinutes: Boolean): String {
        val skeleton = when {
            use24Hour && withMinutes -> "Hm"
            use24Hour -> "H"
            else -> "hm"
        }
        val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)
        // An hour-only 12-hour clock still needs the day-period marker, so start from "hm" and drop the
        // minutes rather than asking for "h" alone, which carries no marker.
        return if (!use24Hour && !withMinutes) MINUTE_FIELD.replace(pattern, "").trim() else pattern
    }
}

/** The place's zone, or the device zone when the geocoder gave one Java does not know. */
fun Place.zoneId(): ZoneId = runCatching { ZoneId.of(timeZone) }.getOrDefault(ZoneId.systemDefault())