package com.example.nimbus.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nimbus.domain.model.WeatherCondition
import com.example.nimbus.ui.theme.NimbusTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A weather glyph drawn on a Canvas: sun, moon, clouds, rain, snow, fog and lightning composed per
 * [condition]. When [animated], one looping phase drives every motion (rays turn, drops fall, the bolt
 * flashes); the phase is only read inside the draw pass, so the animation never recomposes anything.
 *
 * Clouds, fog and snow take [cloudColor] so the same glyph works as white on the sky and as
 * `onSurfaceVariant` on a card.
 */
@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    cloudColor: Color = LocalContentColor.current,
) {
    val phase: State<Float> = if (animated) {
        rememberInfiniteTransition(label = "weather-icon").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 3600, easing = LinearEasing)),
            label = "phase",
        )
    } else {
        remember { mutableFloatStateOf(0.35f) }
    }
    Canvas(modifier) {
        drawWeather(condition, isDay, phase.value, cloudColor)
    }
}

private val SunColor = Color(0xFFFFC857)
private val SunCore = Color(0xFFFFE08A)
private val RainColor = Color(0xFF6EC6FF)
private val BoltColor = Color(0xFFFFE082)

private fun DrawScope.drawWeather(condition: WeatherCondition, isDay: Boolean, phase: Float, cloudColor: Color) {
    val s = size.minDimension
    val c = Offset(size.width / 2f, size.height / 2f)
    val dimCloud = cloudColor.copy(alpha = cloudColor.alpha * 0.55f)
    when (condition) {
        WeatherCondition.CLEAR -> drawCelestial(c, s * 0.30f, isDay, phase, cloudColor)
        WeatherCondition.MAINLY_CLEAR -> {
            drawCelestial(Offset(c.x - s * 0.06f, c.y - s * 0.06f), s * 0.27f, isDay, phase, cloudColor)
            drawCloud(Offset(c.x + s * 0.2f, c.y + s * 0.22f), s * 0.13f, cloudColor)
        }
        WeatherCondition.PARTLY_CLOUDY -> {
            drawCelestial(Offset(c.x + s * 0.16f, c.y - s * 0.16f), s * 0.21f, isDay, phase, cloudColor)
            drawCloud(Offset(c.x - s * 0.06f, c.y + s * 0.12f), s * 0.2f, cloudColor)
        }
        WeatherCondition.OVERCAST -> {
            drawCloud(Offset(c.x + s * 0.14f, c.y - s * 0.1f), s * 0.16f, dimCloud)
            drawCloud(Offset(c.x - s * 0.06f, c.y + s * 0.1f), s * 0.21f, cloudColor)
        }
        WeatherCondition.FOG -> {
            drawCloud(Offset(c.x, c.y - s * 0.14f), s * 0.18f, cloudColor)
            drawFog(c, s, phase, cloudColor)
        }
        WeatherCondition.DRIZZLE -> {
            drawCloud(Offset(c.x, c.y - s * 0.1f), s * 0.2f, cloudColor)
            drawRain(c, s, phase, count = 3, length = s * 0.07f, spread = 0.6f)
        }
        WeatherCondition.RAIN -> {
            drawCloud(Offset(c.x, c.y - s * 0.1f), s * 0.2f, cloudColor)
            drawRain(c, s, phase, count = 4, length = s * 0.13f, spread = 1f)
        }
        WeatherCondition.FREEZING_RAIN -> {
            drawCloud(Offset(c.x, c.y - s * 0.1f), s * 0.2f, cloudColor)
            drawRain(Offset(c.x - s * 0.07f, c.y), s, phase, count = 2, length = s * 0.12f, spread = 2f)
            drawSnow(Offset(c.x + s * 0.07f, c.y), s, phase, count = 2, color = cloudColor)
        }
        WeatherCondition.SNOW -> {
            drawCloud(Offset(c.x, c.y - s * 0.1f), s * 0.2f, cloudColor)
            drawSnow(c, s, phase, count = 4, color = cloudColor)
        }
        WeatherCondition.SHOWERS -> {
            drawCelestial(Offset(c.x + s * 0.18f, c.y - s * 0.2f), s * 0.17f, isDay, phase, cloudColor)
            drawCloud(Offset(c.x - s * 0.04f, c.y - s * 0.04f), s * 0.2f, cloudColor)
            drawRain(c, s, phase, count = 3, length = s * 0.11f, spread = 1f)
        }
        WeatherCondition.THUNDERSTORM -> {
            drawCloud(Offset(c.x, c.y - s * 0.12f), s * 0.21f, cloudColor)
            drawRain(c, s, phase, count = 2, length = s * 0.1f, spread = 2.2f)
            drawBolt(Offset(c.x, c.y + s * 0.18f), s * 1.35f, phase)
        }
    }
}

/** The sun by day; by night a moon in [moonColor], which is the cloud colour so it stays visible on a card. */
private fun DrawScope.drawCelestial(center: Offset, radius: Float, isDay: Boolean, phase: Float, moonColor: Color) {
    if (isDay) drawSun(center, radius, phase) else drawMoon(center, radius, moonColor)
}

private fun DrawScope.drawSun(center: Offset, radius: Float, phase: Float) {
    val rayWidth = (radius * 0.2f).coerceAtLeast(1.5f)
    // Rays sit every 45°, so a quarter turn per cycle loops seamlessly.
    rotate(degrees = phase * 90f, pivot = center) {
        for (i in 0 until 8) {
            val angle = i * (PI.toFloat() / 4f)
            val direction = Offset(cos(angle), sin(angle))
            drawLine(
                color = SunColor,
                start = center + direction * (radius * 1.35f),
                end = center + direction * (radius * 1.7f),
                strokeWidth = rayWidth,
                cap = StrokeCap.Round,
            )
        }
    }
    drawCircle(SunColor, radius, center)
    drawCircle(SunCore, radius * 0.72f, center)
}

private fun DrawScope.drawMoon(center: Offset, radius: Float, color: Color) {
    val disc = Path().apply {
        addOval(Rect(center - Offset(radius, radius), Size(radius * 2f, radius * 2f)))
    }
    val biteRadius = radius * 0.85f
    val biteCenter = center + Offset(radius * 0.55f, -radius * 0.45f)
    val bite = Path().apply {
        addOval(Rect(biteCenter - Offset(biteRadius, biteRadius), Size(biteRadius * 2f, biteRadius * 2f)))
    }
    drawPath(Path.combine(PathOperation.Difference, disc, bite), color)
}

/** Three lobes over a flat base; [r] is the radius of the tallest lobe, the whole cloud is about 3.7r wide. */
private fun DrawScope.drawCloud(center: Offset, r: Float, color: Color) {
    val baseY = center.y + r * 0.6f
    drawCircle(color, r * 0.7f, Offset(center.x - r * 1.05f, baseY - r * 0.7f))
    drawCircle(color, r, Offset(center.x - r * 0.1f, baseY - r))
    drawCircle(color, r * 0.8f, Offset(center.x + r * 1.1f, baseY - r * 0.8f))
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - r * 1.05f, baseY - r * 0.8f),
        size = Size(r * 2.15f, r * 0.8f),
        cornerRadius = CornerRadius(r * 0.3f),
    )
}

private fun DrawScope.drawRain(center: Offset, s: Float, phase: Float, count: Int, length: Float, spread: Float) {
    val spacing = s * 0.14f * spread
    val startX = center.x - spacing * (count - 1) / 2f
    val top = center.y + s * 0.14f
    val travel = s * 0.2f
    val stroke = (s * 0.045f).coerceAtLeast(1.5f)
    for (i in 0 until count) {
        val t = (phase + i * 0.29f) % 1f
        val x = startX + i * spacing
        val y = top + t * travel
        val alpha = (1.2f - t).coerceIn(0.2f, 1f)
        drawLine(
            color = RainColor.copy(alpha = alpha),
            start = Offset(x + length * 0.3f, y),
            end = Offset(x, y + length),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawSnow(center: Offset, s: Float, phase: Float, count: Int, color: Color) {
    val spacing = s * 0.14f
    val startX = center.x - spacing * (count - 1) / 2f
    val top = center.y + s * 0.16f
    val travel = s * 0.2f
    val radius = (s * 0.035f).coerceAtLeast(1.2f)
    for (i in 0 until count) {
        val t = (phase * 0.7f + i * 0.31f) % 1f
        val sway = sin((phase * 2f + i) * (PI.toFloat() * 2f)) * s * 0.02f
        val alpha = (1.2f - t).coerceIn(0.25f, 1f)
        drawCircle(color.copy(alpha = alpha), radius, Offset(startX + i * spacing + sway, top + t * travel))
    }
}

private fun DrawScope.drawFog(center: Offset, s: Float, phase: Float, color: Color) {
    val stroke = (s * 0.05f).coerceAtLeast(1.5f)
    for (i in 0 until 3) {
        val y = center.y + s * (0.16f + i * 0.11f)
        val shift = sin((phase + i * 0.33f) * 2f * PI.toFloat()) * s * 0.05f
        val halfWidth = s * (0.26f - i * 0.04f)
        drawLine(
            color = color.copy(alpha = (0.85f - i * 0.2f) * color.alpha),
            start = Offset(center.x - halfWidth + shift, y),
            end = Offset(center.x + halfWidth + shift, y),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawBolt(anchor: Offset, s: Float, phase: Float) {
    // Two quick flashes per cycle, dim in between.
    val flash = if (phase in 0.10f..0.16f || phase in 0.22f..0.30f) 1f else 0.45f
    val bolt = Path().apply {
        moveTo(anchor.x + s * 0.02f, anchor.y - s * 0.12f)
        lineTo(anchor.x - s * 0.07f, anchor.y + s * 0.02f)
        lineTo(anchor.x - s * 0.005f, anchor.y + s * 0.02f)
        lineTo(anchor.x - s * 0.05f, anchor.y + s * 0.16f)
        lineTo(anchor.x + s * 0.07f, anchor.y - s * 0.02f)
        lineTo(anchor.x + s * 0.005f, anchor.y - s * 0.02f)
        close()
    }
    drawPath(bolt, BoltColor.copy(alpha = flash))
}

@Preview(showBackground = true)
@Composable
private fun WeatherIconGalleryPreview() {
    NimbusTheme(dynamicColor = false) {
        Surface(color = Color(0xFF3A7BD5)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                WeatherCondition.entries.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { condition ->
                            WeatherIcon(condition, isDay = true, modifier = Modifier.size(64.dp), animated = false, cloudColor = Color.White)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WeatherIcon(WeatherCondition.CLEAR, isDay = false, modifier = Modifier.size(64.dp), animated = false, cloudColor = Color.White)
                    WeatherIcon(WeatherCondition.PARTLY_CLOUDY, isDay = false, modifier = Modifier.size(64.dp), animated = false, cloudColor = Color.White)
                    WeatherIcon(WeatherCondition.SNOW, isDay = false, modifier = Modifier.size(64.dp), animated = false, cloudColor = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
