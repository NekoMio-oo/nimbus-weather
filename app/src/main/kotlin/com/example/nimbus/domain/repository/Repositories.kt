package com.example.nimbus.domain.repository

import com.example.nimbus.domain.model.Forecast
import com.example.nimbus.domain.model.Place
import com.example.nimbus.domain.model.UnitSystem
import kotlinx.coroutines.flow.Flow

/** The places the user follows, and the geocoder that finds new ones. */
interface PlacesRepository {
    /** Saved places in the order they were added; emits again whenever the list changes. */
    val savedPlaces: Flow<List<Place>>

    /** Places matching a free-text [query], best match first. Throws a ForecastException on failure. */
    suspend fun search(query: String): List<Place>

    suspend fun save(place: Place)

    suspend fun remove(place: Place)
}

/** Forecasts, with the last successful answer kept per place so the app opens with something to show. */
interface WeatherRepository {
    /** The most recent forecast stored for [place], however old, or null if none was ever fetched. */
    fun cached(place: Place): Forecast?

    /** Fetch a fresh forecast and remember it. Throws a ForecastException on failure. */
    suspend fun fetch(place: Place): Forecast
}

/** User preferences. */
interface SettingsRepository {
    val unitSystem: Flow<UnitSystem>

    suspend fun setUnitSystem(unitSystem: UnitSystem)
}
