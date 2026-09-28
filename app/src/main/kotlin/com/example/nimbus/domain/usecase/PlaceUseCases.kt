package com.example.nimbus.domain.usecase

import com.example.nimbus.domain.model.Place
import com.example.nimbus.domain.repository.PlacesRepository
import kotlinx.coroutines.flow.Flow

class ObserveSavedPlacesUseCase(private val places: PlacesRepository) {
    operator fun invoke(): Flow<List<Place>> = places.savedPlaces
}

class SearchPlacesUseCase(private val places: PlacesRepository) {
    /** Search for [query], or nothing when it is too short to be worth a round trip. */
    suspend operator fun invoke(query: String): List<Place> {
        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) return emptyList()
        return places.search(trimmed)
    }

    companion object {
        const val MIN_QUERY_LENGTH = 2
    }
}

class SavePlaceUseCase(private val places: PlacesRepository) {
    suspend operator fun invoke(place: Place) = places.save(place)
}

class RemovePlaceUseCase(private val places: PlacesRepository) {
    suspend operator fun invoke(place: Place) = places.remove(place)
}
