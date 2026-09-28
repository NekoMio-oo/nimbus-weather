package com.example.nimbus.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nimbus.R
import com.example.nimbus.domain.model.LifeIndex
import com.example.nimbus.ui.components.Eyebrow
import com.example.nimbus.ui.components.SectionCard
import com.example.nimbus.ui.components.lifeIndexLabel
import com.example.nimbus.ui.preview.PreviewData
import com.example.nimbus.ui.theme.NimbusTheme

/**
 * The day's life indices — what to wear, whether to take an umbrella, whether it is a good day to wash the
 * car — as a strip of chips in the order the domain model ranks them.
 */
@Composable
fun LifeIndicesCard(indices: List<LifeIndex>, modifier: Modifier = Modifier) {
    if (indices.isEmpty()) return
    SectionCard(title = stringResource(R.string.section_life_indices), modifier = modifier) {
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            indices.forEach { index -> LifeIndexChip(index) }
        }
    }
}

@Composable
private fun LifeIndexChip(index: LifeIndex) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Eyebrow(lifeIndexLabel(index.type))
            Text(
                index.level,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (index.brief.isNotBlank() && index.brief != index.level) {
                Text(index.brief, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LifeIndicesCardPreview() {
    NimbusTheme(dynamicColor = false) {
        LifeIndicesCard(PreviewData.forecast.lifeIndices, Modifier.padding(16.dp))
    }
}