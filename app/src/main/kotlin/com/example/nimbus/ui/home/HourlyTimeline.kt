package com.example.nimbus.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nimbus.R
import com.example.nimbus.domain.model.Forecast
import com.example.nimbus.domain.model.HourlyForecast
import com.example.nimbus.domain.model.UnitSystem
import com.example.nimbus.ui.components.WeatherIcon
import com.example.nimbus.util.Formatters

private val HourSlot: Dp = 64.dp

/**
 * The next 24 hours as one horizontally scrolling strip: a temperature curve that draws itself in, with
 * the hour, glyph and rain chance for each column lined up underneath it.
 */
@Composable
fun HourlyTimeline(
    forecast: Forecast,
    unitSystem: UnitSystem,
    modifier: Modifier = Modifier,
) {
    val hours = forecast.hourly
    if (hours.isEmpty()) return
    Column(modifier.horizontalScroll(rememberScrollState())) {
        TemperatureCurve(
            hours = hours,
            unitSystem = unitSystem,
            modifier = Modifier.width(HourSlot * hours.size).height(96.dp),
        )
        Spacer(Modifier.height(6.dp))
        Row {
            hours.forEachIndexed { index, hour ->
                HourColumn(
                    hour = hour,
                    isNow = index == 0,
                    isDay = forecast.isDaylight(hour.time),
                    modifier = Modifier.width(HourSlot),
                )
            }
        }
    }
}

@Composable
private fun HourColumn(
    hour: HourlyForecast,
    isNow: Boolean,
    isDay: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            if (isNow) stringResource(R.string.hourly_now) else Formatters.hour(hour.time),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        WeatherIcon(
            condition = hour.condition,
            isDay = isDay,
            modifier = Modifier.size(28.dp),
            animated = false,
            cloudColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (hour.precipitationChance >= 10) "${hour.precipitationChance}%" else " ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/** A smooth curve through the hourly temperatures that reveals itself left to right, with labels every third hour. */
@Composable
private fun TemperatureCurve(
    hours: List<HourlyForecast>,
    unitSystem: UnitSystem,
    modifier: Modifier = Modifier,
) {
    val reveal = remember(hours) { Animatable(0f) }
    LaunchedEffect(hours) {
        reveal.animateTo(1f, tween(durationMillis = 1400, easing = FastOutSlowInEasing))
    }
    val lineColor = MaterialTheme.colorScheme.primary
    val fillColor = lineColor.copy(alpha = 0.16f)
    val labelStyle = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val measurer = rememberTextMeasurer()

    Canvas(modifier) {
        if (hours.size < 2) return@Canvas
        val slot = HourSlot.toPx()
        val temps = hours.map { it.temperatureC }
        val min = temps.min()
        val range = (temps.max() - min).coerceAtLeast(1.0)
        val topPad = 28.dp.toPx()
        val bottomPad = 10.dp.toPx()
        val usable = size.height - topPad - bottomPad
        val points = temps.mapIndexed { i, t ->
            Offset(slot * (i + 0.5f), topPad + (1.0 - (t - min) / range).toFloat() * usable)
        }

        val curve = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.lastIndex) {
                val from = points[i]
                val to = points[i + 1]
                val midX = (from.x + to.x) / 2f
                cubicTo(midX, from.y, midX, to.y, to.x, to.y)
            }
        }
        val measure = PathMeasure().apply { setPath(curve, false) }
        val revealedLength = measure.length * reveal.value
        val revealed = Path()
        measure.getSegment(0f, revealedLength, revealed, true)
        val end = measure.getPosition(revealedLength)

        val area = Path().apply {
            addPath(revealed)
            lineTo(end.x, size.height)
            lineTo(points.first().x, size.height)
            close()
        }
        drawPath(area, Brush.verticalGradient(listOf(fillColor, Color.Transparent)))
        drawPath(
            revealed,
            lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        points.forEachIndexed { i, point ->
            if (i % 3 != 0) return@forEachIndexed
            val alpha = ((reveal.value * points.size - i) / 2f).coerceIn(0f, 1f)
            if (alpha <= 0f) return@forEachIndexed
            val label = measurer.measure(Formatters.temperature(temps[i], unitSystem), labelStyle)
            drawText(
                textLayoutResult = label,
                topLeft = Offset(point.x - label.size.width / 2f, point.y - label.size.height - 8.dp.toPx()),
                alpha = alpha,
            )
            drawCircle(lineColor, radius = 3.5.dp.toPx(), center = point, alpha = alpha)
        }
    }
}
