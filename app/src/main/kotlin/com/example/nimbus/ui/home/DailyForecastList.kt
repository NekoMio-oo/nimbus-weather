package com.example.nimbus.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nimbus.R
import com.example.nimbus.domain.model.DailyForecast
import com.example.nimbus.domain.model.UnitSystem
import com.example.nimbus.ui.components.SectionCard
import com.example.nimbus.ui.components.WeatherIcon
import com.example.nimbus.ui.components.uvLabel
import com.example.nimbus.ui.preview.PreviewData
import com.example.nimbus.ui.theme.NimbusTheme
import com.example.nimbus.util.Formatters
import java.time.LocalDate

/**
 * The week ahead, one row per day. Each row's temperature bar sits on the week's overall range so the days
 * compare at a glance; tapping a row expands sunrise, sunset and UV for that day.
 */
@Composable
fun DailyForecastList(
    days: List<DailyForecast>,
    unitSystem: UnitSystem,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    if (days.isEmpty()) return
    val weekMin = days.minOf { it.minTemperatureC }
    val weekMax = days.maxOf { it.maxTemperatureC }
    var expandedDay by rememberSaveable { mutableStateOf<String?>(null) }

    Column(modifier) {
        days.forEachIndexed { index, day ->
            val key = day.date.toString()
            DailyRow(
                day = day,
                unitSystem = unitSystem,
                isToday = day.date == today,
                weekMin = weekMin,
                weekMax = weekMax,
                expanded = expandedDay == key,
                onToggle = { expandedDay = if (expandedDay == key) null else key },
            )
            if (index < days.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun DailyRow(
    day: DailyForecast,
    unitSystem: UnitSystem,
    isToday: Boolean,
    weekMin: Double,
    weekMax: Double,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onToggle)
            .padding(vertical = 10.dp, horizontal = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (isToday) stringResource(R.string.day_today) else Formatters.weekday(day.date),
                modifier = Modifier.width(56.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
            WeatherIcon(
                condition = day.condition,
                isDay = true,
                modifier = Modifier.size(28.dp),
                animated = false,
                cloudColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (day.precipitationChance >= 10) "${day.precipitationChance}%" else "",
                modifier = Modifier.width(40.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                Formatters.temperature(day.minTemperatureC, unitSystem),
                modifier = Modifier.width(36.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TemperatureRangeBar(
                min = day.minTemperatureC,
                max = day.maxTemperatureC,
                weekMin = weekMin,
                weekMax = weekMax,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp).height(6.dp),
            )
            Text(
                Formatters.temperature(day.maxTemperatureC, unitSystem),
                modifier = Modifier.width(36.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        AnimatedVisibility(expanded) {
            Row(
                Modifier.padding(top = 10.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DetailChip(stringResource(R.string.daily_sunrise, Formatters.clock(day.sunrise)))
                DetailChip(stringResource(R.string.daily_sunset, Formatters.clock(day.sunset)))
                DetailChip(stringResource(R.string.daily_uv, uvLabel(day.uvIndexMax)))
            }
        }
    }
}

@Composable
private fun DetailChip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

/** The day's min..max as a bar positioned within the week's min..max, growing in when first shown. */
@Composable
private fun TemperatureRangeBar(
    min: Double,
    max: Double,
    weekMin: Double,
    weekMax: Double,
    modifier: Modifier = Modifier,
) {
    val span = (weekMax - weekMin).coerceAtLeast(1.0)
    val start = ((min - weekMin) / span).toFloat().coerceIn(0f, 1f)
    val end = ((max - weekMin) / span).toFloat().coerceIn(0f, 1f)
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val animatedEnd by animateFloatAsState(
        targetValue = if (shown) end else start,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "range-end",
    )
    val track = MaterialTheme.colorScheme.surfaceVariant
    val cool = MaterialTheme.colorScheme.primary
    val warm = MaterialTheme.colorScheme.tertiary

    Canvas(modifier) {
        val radius = CornerRadius(size.height / 2f)
        drawRoundRect(track, cornerRadius = radius)
        val x0 = size.width * start
        val x1 = size.width * animatedEnd
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(cool, warm), startX = x0, endX = x1.coerceAtLeast(x0 + 1f)),
            topLeft = Offset(x0, 0f),
            size = Size((x1 - x0).coerceAtLeast(size.height), size.height),
            cornerRadius = radius,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DailyForecastListPreview() {
    NimbusTheme(dynamicColor = false) {
        SectionCard("7-day forecast", Modifier.padding(16.dp)) {
            DailyForecastList(PreviewData.forecast.daily, UnitSystem.METRIC, today = PreviewData.forecast.daily.first().date)
        }
    }
}
