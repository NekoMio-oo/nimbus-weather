package com.example.nimbus.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nimbus.R
import com.example.nimbus.domain.model.DailyForecast
import com.example.nimbus.ui.components.Eyebrow
import com.example.nimbus.ui.components.SectionCard
import com.example.nimbus.util.Formatters
import java.time.Duration
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Today's daylight as an arc from sunrise to sunset, with the sun where it is right now. */
@Composable
fun SunCard(
    day: DailyForecast,
    now: LocalTime,
    modifier: Modifier = Modifier,
) {
    val total = day.daylight.seconds.toFloat().coerceAtLeast(1f)
    val elapsed = Duration.between(day.sunrise, now).seconds.toFloat()
    val target = (elapsed / total).coerceIn(0f, 1f)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(target) {
        progress.animateTo(target, tween(durationMillis = 1400, easing = FastOutSlowInEasing))
    }
    val track = MaterialTheme.colorScheme.surfaceVariant
    val arc = MaterialTheme.colorScheme.tertiary
    val horizon = MaterialTheme.colorScheme.outlineVariant

    SectionCard(title = null, modifier = modifier) {
        Canvas(Modifier.fillMaxWidth().height(96.dp)) {
            val stroke = 4.dp.toPx()
            val inset = 14.dp.toPx()
            val radius = minOf(size.width / 2f - inset, size.height - inset - stroke)
            val center = Offset(size.width / 2f, size.height - stroke)
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2f, radius * 2f)
            drawArc(
                color = track,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(
                    width = stroke,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 8.dp.toPx())),
                ),
            )
            val sweep = 180f * progress.value
            if (sweep > 0f) {
                drawArc(
                    color = arc,
                    startAngle = 180f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
            drawLine(horizon, Offset(0f, center.y), Offset(size.width, center.y), strokeWidth = 1.dp.toPx())
            val angle = (180.0 + sweep) * PI / 180.0
            val sun = Offset(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat())
            drawCircle(arc.copy(alpha = 0.3f), radius = 12.dp.toPx(), center = sun)
            drawCircle(Color(0xFFFFC857), radius = 7.dp.toPx(), center = sun)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Eyebrow(stringResource(R.string.sun_sunrise))
                Text(Formatters.clock(day.sunrise), style = MaterialTheme.typography.titleMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Eyebrow(stringResource(R.string.sun_sunset))
                Text(Formatters.clock(day.sunset), style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.sun_daylight, durationLabel(day.daylight)),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun durationLabel(duration: Duration): String =
    stringResource(R.string.duration_hours_minutes, duration.toHours().toInt(), (duration.toMinutes() % 60).toInt())
