package com.example.nimbus.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/** A placeholder block with a highlight sweeping across it, for content that is still loading. */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    baseColor: Color = Color.White.copy(alpha = 0.18f),
    highlightColor: Color = Color.White.copy(alpha = 0.36f),
) {
    val sweep = rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1400, easing = LinearEasing)),
        label = "sweep",
    )
    Box(
        modifier
            .clip(shape)
            .drawBehind {
                val w = size.width
                val x = w * sweep.value
                drawRect(
                    Brush.linearGradient(
                        listOf(baseColor, highlightColor, baseColor),
                        start = Offset(x, 0f),
                        end = Offset(x + w, size.height),
                    ),
                )
            },
    )
}
