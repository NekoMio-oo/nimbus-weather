package com.example.nimbus.domain.model

import java.time.LocalDateTime

/**
 * The next couple of hours of precipitation, sampled every two minutes by the weather service. Only Chinese
 * cities report this, so the whole block is absent elsewhere and the UI hides the section.
 */
data class MinutelyPrecipitation(
    /** The service's own sentence, e.g. "20分钟左右雨渐停，不过一个半小时后还会下雨". */
    val summary: String?,
    val updateTime: LocalDateTime?,
    val points: List<PrecipitationPoint>,
) {
    /** The strongest intensity in the window, in mm per hour. */
    val peakMmPerHour: Double get() = points.maxOfOrNull { it.mmPerHour } ?: 0.0

    /** True while any precipitation is expected in the window. */
    val hasPrecipitation: Boolean get() = peakMmPerHour > 0.0
}

/** One sample of the minute-level radar: [mmPerHour] is the intensity and [type] is "rain" or "snow". */
data class PrecipitationPoint(
    val time: LocalDateTime,
    val mmPerHour: Double,
    val type: String,
)