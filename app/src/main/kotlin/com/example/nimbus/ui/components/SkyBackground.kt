package com.example.nimbus.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.nimbus.domain.model.WeatherCondition

/** The colours of a sky: a vertical gradient plus a soft glow that drifts across it. */
data class SkyPalette(val top: Color, val bottom: Color, val glow: Color)

/** The sky for a condition. Deliberately not theme tokens: the sky is the weather, not the brand. */
fun skyPalette(condition: WeatherCondition, isDay: Boolean): SkyPalette = if (!isDay) {
    when (condition) {
        WeatherCondition.THUNDERSTORM -> SkyPalette(Color(0xFF14122B), Color(0xFF2E2A52), Color(0xFFFFE082))
        WeatherCondition.RAIN, WeatherCondition.SHOWERS, WeatherCondition.DRIZZLE, WeatherCondition.FREEZING_RAIN ->
            SkyPalette(Color(0xFF0E1726), Color(0xFF243447), Color(0xFF4FC3F7))
        WeatherCondition.SNOW -> SkyPalette(Color(0xFF1B2633), Color(0xFF3B4A5C), Color(0xFFCFD8DC))
        WeatherCondition.FOG, WeatherCondition.OVERCAST -> SkyPalette(Color(0xFF1A2230), Color(0xFF35404F), Color(0xFF90A4AE))
        WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR, WeatherCondition.PARTLY_CLOUDY ->
            SkyPalette(Color(0xFF0B1026), Color(0xFF1F2A55), Color(0xFF7986CB))
    }
} else {
    when (condition) {
        WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR -> SkyPalette(Color(0xFF1E6FE8), Color(0xFF63C7F2), Color(0xFFFFD166))
        WeatherCondition.PARTLY_CLOUDY -> SkyPalette(Color(0xFF3A7BD5), Color(0xFF8FC7EE), Color(0xFFFFE29A))
        WeatherCondition.OVERCAST -> SkyPalette(Color(0xFF5C7C99), Color(0xFF9FB3C8), Color(0xFFDDE6EE))
        WeatherCondition.FOG -> SkyPalette(Color(0xFF7B8FA1), Color(0xFFB8C6D1), Color(0xFFEFF3F6))
        WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.SHOWERS ->
            SkyPalette(Color(0xFF3F5B78), Color(0xFF6F8FA8), Color(0xFF9BE3FF))
        WeatherCondition.FREEZING_RAIN, WeatherCondition.SNOW -> SkyPalette(Color(0xFF7D93A8), Color(0xFFC9D6E2), Color(0xFFFFFFFF))
        WeatherCondition.THUNDERSTORM -> SkyPalette(Color(0xFF2B2D4E), Color(0xFF5B5F8A), Color(0xFFFFE082))
    }
}

/**
 * The full-screen backdrop. Colours cross-fade when the condition changes, tinted a little toward the
 * theme's primary so a Material You palette shows through, and two soft glows drift slowly so the sky is
 * never quite still.
 */
@Composable
fun SkyBackground(
    condition: WeatherCondition,
    isDay: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = skyPalette(condition, isDay)
    val tint = MaterialTheme.colorScheme.primary
    val top by animateColorAsState(lerp(palette.top, tint, 0.18f), tween(durationMillis = 900), label = "sky-top")
    val bottom by animateColorAsState(lerp(palette.bottom, tint, 0.10f), tween(durationMillis = 900), label = "sky-bottom")
    val glow by animateColorAsState(palette.glow, tween(durationMillis = 900), label = "sky-glow")
    val drift = rememberInfiniteTransition(label = "sky-drift").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 16_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift",
    )
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(top, bottom)))
        val d = drift.value
        val glowRadius = size.maxDimension * 0.5f
        val glowCenter = Offset(size.width * (0.15f + 0.7f * d), size.height * 0.12f)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(glow.copy(alpha = 0.45f), Color.Transparent),
                center = glowCenter,
                radius = glowRadius,
            ),
            radius = glowRadius,
            center = glowCenter,
        )
        val hazeRadius = size.maxDimension * 0.42f
        val hazeCenter = Offset(size.width * (0.9f - 0.6f * d), size.height * 0.58f)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(bottom.copy(alpha = 0.55f), Color.Transparent),
                center = hazeCenter,
                radius = hazeRadius,
            ),
            radius = hazeRadius,
            center = hazeCenter,
        )
    }
}
