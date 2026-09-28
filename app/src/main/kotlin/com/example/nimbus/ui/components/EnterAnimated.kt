package com.example.nimbus.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import kotlinx.coroutines.delay

/**
 * Fades and lifts its content into place, [index] steps after the first. Visibility is saved so an item
 * that scrolls out of a list and back does not replay its entrance.
 *
 * In a `@Preview` the content starts already visible: a preview renders a static frame, so an entrance that
 * begins at zero alpha would show an empty screen and hide the very layout you are trying to look at.
 */
@Composable
fun EnterAnimated(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val inPreview = LocalInspectionMode.current
    var visible by rememberSaveable { mutableStateOf(inPreview) }
    LaunchedEffect(Unit) {
        if (visible) return@LaunchedEffect
        delay(index * STAGGER_MS)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(durationMillis = 420)) + slideInVertically(tween(durationMillis = 420)) { it / 6 },
    ) {
        content()
    }
}

private const val STAGGER_MS = 70L
