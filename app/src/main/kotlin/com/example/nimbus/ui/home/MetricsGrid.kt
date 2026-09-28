package com.example.nimbus.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nimbus.R
import com.example.nimbus.domain.model.CurrentConditions
import com.example.nimbus.domain.model.DailyForecast
import com.example.nimbus.domain.model.UnitSystem
import com.example.nimbus.ui.components.compassLabel
import com.example.nimbus.ui.components.uvLabel
import com.example.nimbus.ui.preview.PreviewData
import com.example.nimbus.ui.theme.NimbusTheme
import com.example.nimbus.util.Formatters

/** Six detail cards in two columns: feels like, humidity, wind, UV, pressure, precipitation. */
@Composable
fun MetricsGrid(
    current: CurrentConditions,
    today: DailyForecast?,
    unitSystem: UnitSystem,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = stringResource(R.string.metric_feels_like),
                value = Formatters.temperature(current.apparentTemperatureC, unitSystem),
                caption = feelsLikeCaption(current),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = stringResource(R.string.metric_humidity),
                value = "${current.humidityPercent}%",
                caption = stringResource(R.string.metric_humidity_caption),
                modifier = Modifier.weight(1f),
            ) { HumidityRing(current.humidityPercent) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = stringResource(R.string.metric_wind),
                value = Formatters.speed(current.windSpeedKmh, unitSystem),
                caption = stringResource(R.string.metric_wind_caption, compassLabel(current.windDirectionDegrees)),
                modifier = Modifier.weight(1f),
            ) { WindArrow(current.windDirectionDegrees) }
            MetricCard(
                title = stringResource(R.string.metric_uv),
                value = Formatters.uvIndex(current.uvIndex),
                caption = uvLabel(current.uvIndex),
                modifier = Modifier.weight(1f),
            ) { UvScale(current.uvIndex) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = stringResource(R.string.metric_pressure),
                value = Formatters.pressure(current.pressureHpa, unitSystem),
                caption = stringResource(R.string.metric_pressure_caption),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = stringResource(R.string.metric_precipitation),
                value = Formatters.precipitation(current.precipitationMm, unitSystem),
                caption = if (today != null) stringResource(R.string.metric_precipitation_caption, today.precipitationChance) else "",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun feelsLikeCaption(current: CurrentConditions): String {
    val difference = current.apparentTemperatureC - current.temperatureC
    return stringResource(
        when {
            difference > 1.5 -> R.string.metric_feels_like_warmer
            difference < -1.5 -> R.string.metric_feels_like_cooler
            else -> R.string.metric_feels_like_same
        },
    )
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
    visual: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier.height(124.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (visual != null) visual()
            }
            Spacer(Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Fills up to the humidity when first shown. */
@Composable
private fun HumidityRing(percent: Int) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val progress by animateFloatAsState(
        targetValue = if (shown) percent / 100f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "humidity",
    )
    CircularProgressIndicator(
        progress = { progress },
        modifier = Modifier.size(28.dp),
        strokeWidth = 4.dp,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = StrokeCap.Round,
    )
}

/** Points where the wind is going. Meteorological direction is where it comes from, hence the half turn. */
@Composable
private fun WindArrow(fromDegrees: Int) {
    val rotation by animateFloatAsState(
        targetValue = ((fromDegrees + 180) % 360).toFloat(),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "wind",
    )
    val color = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(28.dp)) {
        rotate(rotation) {
            val c = center
            val h = size.minDimension
            val arrow = Path().apply {
                moveTo(c.x, c.y - h * 0.42f)
                lineTo(c.x + h * 0.28f, c.y + h * 0.36f)
                lineTo(c.x, c.y + h * 0.18f)
                lineTo(c.x - h * 0.28f, c.y + h * 0.36f)
                close()
            }
            drawPath(arrow, color)
        }
    }
}

/** The UV scale from low to extreme with a marker that slides to today's value. */
@Composable
private fun UvScale(uv: Double) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val marker by animateFloatAsState(
        targetValue = if (shown) (uv / 11.0).coerceIn(0.0, 1.0).toFloat() else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "uv",
    )
    Canvas(Modifier.width(56.dp).height(10.dp)) {
        val radius = CornerRadius(size.height / 2f)
        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFF66BB6A), Color(0xFFFFEE58), Color(0xFFFFA726), Color(0xFFEF5350), Color(0xFFAB47BC)),
            ),
            cornerRadius = radius,
        )
        val x = size.height / 2f + (size.width - size.height) * marker
        val center = Offset(x, size.height / 2f)
        drawCircle(Color.White, radius = size.height / 2f, center = center)
        drawCircle(Color.Black.copy(alpha = 0.35f), radius = size.height / 2f, center = center, style = Stroke(1.dp.toPx()))
    }
}

@Preview(showBackground = true)
@Composable
private fun MetricsGridPreview() {
    NimbusTheme(dynamicColor = false) {
        MetricsGrid(PreviewData.forecast.current, PreviewData.forecast.today, UnitSystem.METRIC, Modifier.padding(16.dp))
    }
}
