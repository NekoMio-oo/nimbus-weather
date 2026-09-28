package com.example.nimbus.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nimbus.R
import com.example.nimbus.domain.model.Forecast
import com.example.nimbus.domain.model.UnitSystem
import com.example.nimbus.ui.components.WeatherIcon
import com.example.nimbus.ui.components.conditionLabel
import com.example.nimbus.util.Formatters
import java.time.Duration
import java.time.Instant

/** The big picture: the animated glyph, the temperature, the condition, and how fresh the data is. */
@Composable
fun HeroSection(
    forecast: Forecast,
    unitSystem: UnitSystem,
    now: Instant,
    modifier: Modifier = Modifier,
) {
    val current = forecast.current
    val today = forecast.today
    val feelsLike = stringResource(R.string.home_feels_like, Formatters.temperature(current.apparentTemperatureC, unitSystem))
    val highLow = if (today != null) {
        stringResource(
            R.string.home_high_low,
            Formatters.temperature(today.maxTemperatureC, unitSystem),
            Formatters.temperature(today.minTemperatureC, unitSystem),
        )
    } else {
        null
    }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(
            modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WeatherIcon(current.condition, current.isDay, modifier = Modifier.size(140.dp))
            AnimatedContent(
                targetState = Formatters.temperature(current.temperatureC, unitSystem),
                transitionSpec = {
                    (fadeIn(tween(320)) + slideInVertically(tween(320)) { it / 3 })
                        .togetherWith(fadeOut(tween(180)) + slideOutVertically(tween(180)) { -it / 3 })
                },
                label = "temperature",
            ) { value ->
                // The degree sign has no left-hand counterweight; a little start padding centres the digits.
                Text(value, style = MaterialTheme.typography.displayLarge, modifier = Modifier.padding(start = 14.dp))
            }
            Text(conditionLabel(current.condition, current.isDay), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (highLow != null) "$feelsLike  ·  $highLow" else feelsLike,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                updatedLabel(forecast.fetchedAt, now),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

/** "Updated just now" / "Updated 5 min ago" / "Updated 2 h ago". */
@Composable
fun updatedLabel(fetchedAt: Instant, now: Instant): String {
    val minutes = Duration.between(fetchedAt, now).toMinutes().coerceAtLeast(0L)
    return when {
        minutes < 1 -> stringResource(R.string.home_updated_now)
        minutes < 60 -> stringResource(R.string.home_updated_minutes, minutes.toInt())
        else -> stringResource(R.string.home_updated_hours, (minutes / 60).toInt())
    }
}
