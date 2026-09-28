package com.example.nimbus.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nimbus.R
import com.example.nimbus.domain.model.AirPollutants
import com.example.nimbus.domain.model.AirQuality
import com.example.nimbus.ui.components.Eyebrow
import com.example.nimbus.ui.components.SectionCard
import com.example.nimbus.ui.preview.PreviewData
import com.example.nimbus.ui.theme.NimbusTheme
import kotlin.math.roundToInt

/**
 * Air quality: the index in a coloured disc, the band's name, the pollutant driving it, and the six
 * concentrations behind it. The colours follow China's AQI bands, which is the scale the service reports on.
 */
@Composable
fun AirQualityCard(air: AirQuality, modifier: Modifier = Modifier) {
    SectionCard(title = stringResource(R.string.section_air_quality), modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(aqiColor(air), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    air.aqi.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    air.category ?: stringResource(R.string.aqi_unknown),
                    style = MaterialTheme.typography.titleLarge,
                )
                if (air.hasPrimary) {
                    Text(
                        stringResource(R.string.aqi_primary, air.primary.orEmpty()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        air.pollutants?.let { pollutants ->
            Spacer(Modifier.height(16.dp))
            PollutantGrid(pollutants)
        }
    }
}

/** The six concentrations as a 3×2 grid of label-over-value cells. */
@Composable
private fun PollutantGrid(pollutants: AirPollutants) {
    val cells = listOf(
        stringResource(R.string.pollutant_pm25) to concentration(pollutants.pm25),
        stringResource(R.string.pollutant_pm10) to concentration(pollutants.pm10),
        stringResource(R.string.pollutant_o3) to concentration(pollutants.o3),
        stringResource(R.string.pollutant_no2) to concentration(pollutants.no2),
        stringResource(R.string.pollutant_so2) to concentration(pollutants.so2),
        stringResource(R.string.pollutant_co) to concentration(pollutants.co),
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        cells.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (label, value) ->
                    Column(Modifier.weight(1f)) {
                        Eyebrow(label)
                        Text(value, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

/** µg/m³, rounded; a value the service left out reads as a dash. */
private fun concentration(value: Double?): String = value?.roundToInt()?.toString() ?: "–"

/** The AQI band colour: the service's 1–6 level when given, otherwise the index's own thresholds. */
private fun aqiColor(air: AirQuality): Color {
    val band = air.level ?: when {
        air.aqi <= 50 -> 1
        air.aqi <= 100 -> 2
        air.aqi <= 150 -> 3
        air.aqi <= 200 -> 4
        air.aqi <= 300 -> 5
        else -> 6
    }
    return when (band) {
        1 -> Color(0xFF4CAF50)
        2 -> Color(0xFFCDDC39)
        3 -> Color(0xFFFF9800)
        4 -> Color(0xFFF44336)
        5 -> Color(0xFF9C27B0)
        else -> Color(0xFF7B1FA2)
    }
}

@Preview(showBackground = true)
@Composable
private fun AirQualityCardPreview() {
    NimbusTheme(dynamicColor = false) {
        PreviewData.forecast.airQuality?.let { AirQualityCard(it, Modifier.padding(16.dp)) }
    }
}