package com.example.nimbus.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nimbus.domain.model.Forecast
import com.example.nimbus.domain.model.ForecastException
import com.example.nimbus.domain.model.Place
import com.example.nimbus.domain.model.UnitSystem
import com.example.nimbus.domain.usecase.GetForecastUseCase
import com.example.nimbus.domain.usecase.ObserveSavedPlacesUseCase
import com.example.nimbus.domain.usecase.ObserveUnitSystemUseCase
import com.example.nimbus.domain.usecase.SetUnitSystemUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** What the home screen knows about one place's forecast. */
sealed interface ForecastUiState {
    /** Nothing to show yet: first load, no cached copy. */
    data object Loading : ForecastUiState

    /** A forecast to draw; [isRefreshing] while a newer one is on its way. */
    data class Ready(val forecast: Forecast, val isRefreshing: Boolean = false) : ForecastUiState

    /** The last fetch failed. [cached] is the earlier forecast to keep showing, if there was one. */
    data class Failed(val error: ForecastException, val cached: Forecast?) : ForecastUiState
}

/** The forecast a state can draw, fresh or stale, or null when it has nothing. */
val ForecastUiState.forecastOrNull: Forecast?
    get() = when (this) {
        is ForecastUiState.Ready -> forecast
        is ForecastUiState.Failed -> cached
        ForecastUiState.Loading -> null
    }

data class HomeUiState(
    val places: List<Place> = emptyList(),
    val forecasts: Map<Long, ForecastUiState> = emptyMap(),
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    /** False until the saved places have been read, so an empty list is not mistaken for "none saved". */
    val placesLoaded: Boolean = false,
)

class HomeViewModel(
    observeSavedPlaces: ObserveSavedPlacesUseCase,
    private val getForecast: GetForecastUseCase,
    observeUnitSystem: ObserveUnitSystemUseCase,
    private val setUnitSystem: SetUnitSystemUseCase,
) : ViewModel() {

    private val places: StateFlow<List<Place>?> =
        observeSavedPlaces().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val forecasts = MutableStateFlow<Map<Long, ForecastUiState>>(emptyMap())

    /** One in-flight load per place, so a refresh replaces a load already underway rather than racing it. */
    private val loads = mutableMapOf<Long, Job>()

    val uiState: StateFlow<HomeUiState> = combine(places, forecasts, observeUnitSystem()) { placeList, forecastMap, units ->
        HomeUiState(
            places = placeList ?: emptyList(),
            forecasts = forecastMap,
            unitSystem = units,
            placesLoaded = placeList != null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        viewModelScope.launch {
            places.filterNotNull().collect { list ->
                val ids = list.map { it.id }.toSet()
                list.forEach { place -> if (place.id !in forecasts.value) load(place, forceRefresh = false) }
                forecasts.update { map -> map.filterKeys { it in ids } }
                loads.keys.filterNot { it in ids }.forEach { id -> loads.remove(id)?.cancel() }
            }
        }
    }

    fun refresh(place: Place) = load(place, forceRefresh = true)

    fun toggleUnitSystem() {
        viewModelScope.launch { setUnitSystem(uiState.value.unitSystem.toggled()) }
    }

    private fun load(place: Place, forceRefresh: Boolean) {
        loads[place.id]?.cancel()
        loads[place.id] = viewModelScope.launch {
            val cached = getForecast.cached(place)
            forecasts.update { map ->
                map + (place.id to if (cached != null) ForecastUiState.Ready(cached, isRefreshing = true) else ForecastUiState.Loading)
            }
            getForecast(place, forceRefresh).fold(
                onSuccess = { fresh ->
                    forecasts.update { map -> map + (place.id to ForecastUiState.Ready(fresh)) }
                },
                onFailure = { error ->
                    val reason = error as? ForecastException ?: ForecastException.Unexpected(error)
                    forecasts.update { map -> map + (place.id to ForecastUiState.Failed(reason, cached)) }
                },
            )
        }
    }
}
