package com.example.nimbus.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nimbus.ui.components.ShimmerBox

/** The forecast's silhouette while the first load is in flight, so the layout does not jump when it lands. */
@Composable
fun ForecastSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ShimmerBox(Modifier.size(140.dp), shape = CircleShape)
        Spacer(Modifier.height(12.dp))
        ShimmerBox(Modifier.width(150.dp).height(84.dp))
        Spacer(Modifier.height(12.dp))
        ShimmerBox(Modifier.width(120.dp).height(22.dp))
        Spacer(Modifier.height(8.dp))
        ShimmerBox(Modifier.width(200.dp).height(18.dp))
        Spacer(Modifier.height(28.dp))
        ShimmerBox(Modifier.fillMaxWidth().height(196.dp))
        Spacer(Modifier.height(14.dp))
        ShimmerBox(Modifier.fillMaxWidth().height(340.dp))
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShimmerBox(Modifier.weight(1f).height(124.dp))
            ShimmerBox(Modifier.weight(1f).height(124.dp))
        }
    }
}
