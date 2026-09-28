package com.example.nimbus.data.repository

import com.example.nimbus.data.local.LocalStore
import com.example.nimbus.data.remote.WeatherApi
import com.example.nimbus.domain.model.Place
import com.example.nimbus.domain.repository.PlacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlacesRepositoryImpl(
    private val api: WeatherApi,
    private val store: LocalStore,
) : PlacesRepository {

    override val savedPlaces: Flow<List<Place>> = store.places.map { stored -> stored.map { it.toDomain() } }

    override suspend fun search(query: String): List<Place> = networkCall {
        // The geocoder lists every hamlet that shares a name; one row per (name, region, country) is what a
        // person searching for a city means, and the first of each group is the geocoder's best match.
        api.searchPlaces(query).results
            .distinctBy { Triple(it.name, it.admin1, it.countryCode) }
            .map { it.toDomain() }
    }

    override suspend fun save(place: Place) {
        val current = store.places.value
        if (current.any { it.id == place.id }) return
        store.writePlaces(current + place.toStored())
    }

    override suspend fun remove(place: Place) {
        store.writePlaces(store.places.value.filterNot { it.id == place.id })
        store.removeCachedForecast(place.id)
    }
}
