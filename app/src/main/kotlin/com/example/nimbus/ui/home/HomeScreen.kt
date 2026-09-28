package com.example.nimbus.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nimbus.R
import com.example.nimbus.domain.model.Forecast
import com.example.nimbus.domain.model.ForecastException
import com.example.nimbus.domain.model.Place
import com.example.nimbus.domain.model.UnitSystem
import com.example.nimbus.domain.model.WeatherCondition
import com.example.nimbus.ui.components.EnterAnimated
import com.example.nimbus.ui.components.ErrorState
import com.example.nimbus.ui.components.SectionCard
import com.example.nimbus.ui.components.SkyBackground
import com.example.nimbus.ui.components.StaleBanner
import com.example.nimbus.ui.components.WeatherIcon
import com.example.nimbus.ui.preview.PreviewData
import com.example.nimbus.ui.theme.NimbusTheme
import com.example.nimbus.util.zoneId
import kotlinx.coroutines.delay
import java.time.ZonedDateTime

/** Binds the view model to the stateless [HomeScreen]. */
@Composable
fun HomeRoute(viewModel: HomeViewModel, onOpenSearch: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onOpenSearch = onOpenSearch,
        onRefresh = viewModel::refresh,
        onToggleUnitSystem = viewModel::toggleUnitSystem,
    )
}

/**
 * One page per saved place, swiped horizontally; the sky behind everything follows the page in view.
 * Stateless so it can be previewed with fixed data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpenSearch: () -> Unit,
    onRefresh: (Place) -> Unit,
    onToggleUnitSystem: () -> Unit,
) {
    if (state.places.isEmpty()) {
        EmptyHome(visible = state.placesLoaded, onOpenSearch = onOpenSearch)
        return
    }
    val pagerState = rememberPagerState(pageCount = { state.places.size })

    // A place added from Search lands at the end of the list; scroll to it when the count grows.
    var knownCount by rememberSaveable { mutableIntStateOf(state.places.size) }
    LaunchedEffect(state.places.size) {
        if (state.places.size > knownCount) pagerState.animateScrollToPage(state.places.lastIndex)
        knownCount = state.places.size
    }

    val page = pagerState.currentPage.coerceIn(0, state.places.lastIndex)
    val currentPlace = state.places[page]
    val sky = state.forecasts[currentPlace.id]?.forecastOrNull?.current

    Box(Modifier.fillMaxSize()) {
        SkyBackground(
            condition = sky?.condition ?: WeatherCondition.PARTLY_CLOUDY,
            isDay = sky?.isDay ?: true,
            modifier = Modifier.fillMaxSize(),
        )
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                HomeTopBar(
                    place = currentPlace,
                    pageCount = state.places.size,
                    page = page,
                    unitSystem = state.unitSystem,
                    onOpenSearch = onOpenSearch,
                    onToggleUnitSystem = onToggleUnitSystem,
                )
            },
        ) { padding ->
            // The pager can ask for a page one frame before or after the place list changes under it (a
            // place added or removed on the Search screen), so both lambdas tolerate an index that is
            // momentarily out of range rather than trusting the two to agree within a frame.
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().padding(padding),
                key = { index -> state.places.getOrNull(index)?.id ?: index },
            ) { index ->
                val place = state.places.getOrNull(index) ?: return@HorizontalPager
                ForecastPage(
                    state = state.forecasts[place.id] ?: ForecastUiState.Loading,
                    unitSystem = state.unitSystem,
                    onRefresh = { onRefresh(place) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    place: Place,
    pageCount: Int,
    page: Int,
    unitSystem: UnitSystem,
    onOpenSearch: () -> Unit,
    onToggleUnitSystem: () -> Unit,
) {
    Column {
        CenterAlignedTopAppBar(
            title = {
                AnimatedContent(
                    targetState = place,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                    label = "place-title",
                ) { shown ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            shown.name,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (shown.subtitle.isNotBlank()) {
                            Text(
                                shown.subtitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            },
            navigationIcon = {
                TextButton(
                    onClick = onToggleUnitSystem,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                ) {
                    Text(
                        stringResource(if (unitSystem == UnitSystem.METRIC) R.string.units_celsius else R.string.units_fahrenheit),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            },
            actions = {
                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.action_search))
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White,
                navigationIconContentColor = Color.White,
            ),
        )
        if (pageCount > 1) {
            PagerDots(
                count = pageCount,
                selected = page,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 6.dp),
            )
        }
    }
}

@Composable
private fun PagerDots(count: Int, selected: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { index ->
            val active = index == selected
            val width by animateDpAsState(if (active) 18.dp else 6.dp, label = "dot-width")
            Box(
                Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (active) 0.95f else 0.45f)),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForecastPage(
    state: ForecastUiState,
    unitSystem: UnitSystem,
    onRefresh: () -> Unit,
) {
    val refreshing = state is ForecastUiState.Ready && state.isRefreshing
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        when (state) {
            ForecastUiState.Loading -> ForecastSkeleton(Modifier.fillMaxSize())
            is ForecastUiState.Ready -> ForecastContent(state.forecast, unitSystem, error = null, onRetry = onRefresh)
            is ForecastUiState.Failed -> {
                val cached = state.cached
                if (cached != null) ForecastContent(cached, unitSystem, error = state.error, onRetry = onRefresh)
                else ErrorState(state.error, onRetry = onRefresh, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

/** The forecast itself: hero, hourly timeline, week, details and sun, entering one after another. */
@Composable
fun ForecastContent(
    forecast: Forecast,
    unitSystem: UnitSystem,
    error: ForecastException?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zone = remember(forecast.place.timeZone) { forecast.place.zoneId() }
    // The place's own clock, ticking every half minute so "Updated 3 min ago" and the sun arc stay honest.
    val now by produceState(initialValue = ZonedDateTime.now(zone), zone) {
        while (true) {
            value = ZonedDateTime.now(zone)
            delay(30_000)
        }
    }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val today = forecast.today

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = bottomInset + 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (error != null) {
            item(key = "stale") { StaleBanner(error, onRetry) }
        }
        item(key = "hero") {
            EnterAnimated(0) { HeroSection(forecast, unitSystem, now.toInstant()) }
        }
        forecast.minutely?.let { minutely ->
            item(key = "minutely") {
                EnterAnimated(1) { MinutelyPrecipitationCard(minutely, unitSystem) }
            }
        }
        item(key = "hourly") {
            EnterAnimated(2) {
                SectionCard(stringResource(R.string.section_hourly)) {
                    HourlyTimeline(forecast, unitSystem)
                }
            }
        }
        item(key = "daily") {
            EnterAnimated(3) {
                SectionCard(stringResource(R.string.section_daily)) {
                    DailyForecastList(forecast.daily, unitSystem, today = now.toLocalDate())
                }
            }
        }
        forecast.airQuality?.let { air ->
            item(key = "air") {
                EnterAnimated(4) { AirQualityCard(air) }
            }
        }
        item(key = "metrics") {
            EnterAnimated(5) { MetricsGrid(forecast.current, today, unitSystem) }
        }
        if (forecast.lifeIndices.isNotEmpty()) {
            item(key = "life") {
                EnterAnimated(6) { LifeIndicesCard(forecast.lifeIndices) }
            }
        }
        if (today != null) {
            item(key = "sun") {
                EnterAnimated(7) { SunCard(today, now.toLocalTime()) }
            }
        }
        item(key = "attribution") {
            Text(
                stringResource(R.string.attribution),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** First launch: no places yet. */
@Composable
private fun EmptyHome(visible: Boolean, onOpenSearch: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        SkyBackground(WeatherCondition.PARTLY_CLOUDY, isDay = true, modifier = Modifier.fillMaxSize())
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 8 },
        ) {
            CompositionLocalProvider(LocalContentColor provides Color.White) {
                Column(
                    Modifier
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    WeatherIcon(WeatherCondition.PARTLY_CLOUDY, isDay = true, modifier = Modifier.size(160.dp))
                    Spacer(Modifier.height(24.dp))
                    Text(
                        stringResource(R.string.home_empty_title),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.home_empty_body),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(28.dp))
                    Button(
                        onClick = onOpenSearch,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.home_empty_cta))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ForecastContentPreview() {
    NimbusTheme(dynamicColor = false) {
        Box {
            SkyBackground(WeatherCondition.PARTLY_CLOUDY, isDay = true, modifier = Modifier.fillMaxSize())
            ForecastContent(PreviewData.forecast, UnitSystem.METRIC, error = null, onRetry = {})
        }
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ForecastContentNightPreview() {
    NimbusTheme(dynamicColor = false, darkTheme = true) {
        Box {
            SkyBackground(WeatherCondition.SNOW, isDay = false, modifier = Modifier.fillMaxSize())
            ForecastContent(
                PreviewData.snowyNight,
                UnitSystem.IMPERIAL,
                error = ForecastException.Offline(),
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    NimbusTheme(dynamicColor = false) {
        HomeScreen(
            state = HomeUiState(
                places = PreviewData.places,
                forecasts = mapOf(
                    PreviewData.manila.id to ForecastUiState.Ready(PreviewData.forecast),
                    PreviewData.reykjavik.id to ForecastUiState.Ready(PreviewData.snowyNight),
                ),
                placesLoaded = true,
            ),
            onOpenSearch = {},
            onRefresh = {},
            onToggleUnitSystem = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyHomePreview() {
    NimbusTheme(dynamicColor = false) {
        EmptyHome(visible = true, onOpenSearch = {})
    }
}
