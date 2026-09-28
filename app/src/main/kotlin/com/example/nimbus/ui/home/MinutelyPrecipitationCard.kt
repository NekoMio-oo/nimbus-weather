package com.example.nimbus.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nimbus.R
import com.example.nimbus.domain.model.MinutelyPrecipitation
import com.example.nimbus.domain.model.UnitSystem
import com.example.nimbus.ui.components.SectionCard
import com.example.nimbus.ui.preview.PreviewData
import com.example.nimbus.ui.theme.NimbusTheme
import com.example.nimbus.util.Formatters

/**
 * The next couple of hours of rain, bar by bar. The service samples the radar every two minutes, so the
 * strip reads like a forecast but moves like a clock; the summary sentence above it is the service's own.
 */
@Composable
fun MinutelyPrecipitationCard(
    minutely: MinutelyPrecipitation,
    unitSystem: UnitSystem,
    modifier: Modifier = Modifier,
) {
    val points = minutely.points
    if (points.isEmpty()) return
    val reveal = remember(points) { Animatable(0f) }
    LaunchedEffect(points) { reveal.animateTo(1f, tween(durationMillis = 1200, easing = FastOutSlowInEasing)) }
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    SectionCard(title = stringResource(R.string.section_minutely), modifier = modifier) {
        minutely.summary?.let { summary ->
            Text(summary, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
        }
        Text(
            stringResource(R.string.minutely_peak, Formatters.rainIntensity(minutely.peakMmPerHour, unitSystem)),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Canvas(Modifier.fillMaxWidth().height(76.dp)) {
            val baseline = size.height - 1.dp.toPx()
            val peak = minutely.peakMmPerHour.takeIf { it > 0.0 } ?: 1.0
            val slot = size.width / points.size
            val width = (slot * 0.68f).coerceAtLeast(1f)
            val maxHeight = size.height - 6.dp.toPx()
            drawLine(
                color = trackColor,
                start = Offset(0f, baseline),
                end = Offset(size.width, baseline),
                strokeWidth = 1.dp.toPx(),
            )
            points.forEachIndexed { index, point ->
                val ratio = (point.mmPerHour / peak).toFloat().coerceIn(0f, 1f) * reveal.value
                val height = maxHeight * ratio
                if (height <= 0f) return@forEachIndexed
                val left = index * slot + (slot - width) / 2f
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, baseline - height),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(width / 2f),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(points.first(), points[points.size / 2], points.last()).forEach { point ->
                Text(
                    Formatters.hour(point.time),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MinutelyPrecipitationCardPreview() {
    NimbusTheme(dynamicColor = false) {
        PreviewData.forecast.minutely?.let { minutely ->
            MinutelyPrecipitationCard(minutely, UnitSystem.METRIC)
        }
    }
}