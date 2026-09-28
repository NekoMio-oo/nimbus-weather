package com.example.nimbus.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nimbus.domain.model.ForecastException
import com.example.nimbus.domain.model.Place
import com.example.nimbus.domain.usecase.ObserveSavedPlacesUseCase
import com.example.nimbus.domain.usecase.RemovePlaceUseCase
import com.example.nimbus.domain.usecase.SavePlaceUseCase
import com.example.nimbus.domain.usecase.SearchPlacesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SearchResults {
    /** Query too short to search; the saved places are shown instead. */
    data object Idle : SearchResults

    data object Searching : SearchResults

    data class Found(val places: List<Place>) : SearchResults

    data class Empty(val query: String) : SearchResults

    data class Failed(val error: ForecastException) : SearchResults
}

data class SearchUiState(
    val query: String = "",
    val results: SearchResults = SearchResults.Idle,
    val savedPlaces: List<Place> = emptyList(),
)

class SearchViewModel(
    private val searchPlaces: SearchPlacesUseCase,
    observeSavedPlaces: ObserveSavedPlacesUseCase,
    private val savePlace: SavePlaceUseCase,
    private val removePlace: RemovePlaceUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val attempt = MutableStateFlow(0)
    private val results = MutableStateFlow<SearchResults>(SearchResults.Idle)

    val uiState: StateFlow<SearchUiState> = combine(query, results, observeSavedPlaces()) { q, r, saved ->
        SearchUiState(query = q, results = r, savedPlaces = saved)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    init {
        viewModelScope.launch {
            // collectLatest cancels the search in flight when the user keeps typing; the delay is the debounce.
            combine(query, attempt) { q, a -> q.trim() to a }
                .distinctUntilChanged()
                .collectLatest { (trimmed, _) ->
                    if (trimmed.length < SearchPlacesUseCase.MIN_QUERY_LENGTH) {
                        results.value = SearchResults.Idle
                        return@collectLatest
                    }
                    delay(DEBOUNCE_MS)
                    results.value = SearchResults.Searching
                    results.value = try {
                        val found = searchPlaces(trimmed)
                        if (found.isEmpty()) SearchResults.Empty(trimmed) else SearchResults.Found(found)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: ForecastException) {
                        SearchResults.Failed(e)
                    } catch (e: Exception) {
                        SearchResults.Failed(ForecastException.Unexpected(e))
                    }
                }
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun retry() {
        attempt.value += 1
    }

    /** Save [place] and then run [onSaved] (typically: navigate back to the forecast). */
    fun select(place: Place, onSaved: () -> Unit) {
        viewModelScope.launch {
            savePlace(place)
            onSaved()
        }
    }

    fun remove(place: Place) {
        viewModelScope.launch { removePlace(place) }
    }

    private companion object {
        const val DEBOUNCE_MS = 350L
    }
}
