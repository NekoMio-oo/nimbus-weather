package com.example.nimbus.data.repository

import com.example.nimbus.data.local.LocalStore
import com.example.nimbus.data.remote.CachedForecast
import com.example.nimbus.data.remote.WeatherApi
import com.example.nimbus.domain.model.Forecast
import com.example.nimbus.domain.model.Place
import com.example.nimbus.domain.repository.WeatherRepository
import java.time.Instant

/**
 * Forecasts from the network, with the raw response of the last success written to the local store so the
 * next launch (or a flight) still has something to show. The cache is the wire shape, not the domain one,
 * so a change to the domain model never invalidates what is on disk.
 */
class WeatherRepositoryImpl(
    private val api: WeatherApi,
    private val store: LocalStore,
) : WeatherRepository {

    override fun cached(place: Place): Forecast? {
        val cached = store.cachedForecast(place.id) ?: return null
        return runCatching {
            cached.response.toDomain(place, Instant.ofEpochMilli(cached.fetchedAtEpochMs))
        }.getOrNull()
    }

    override suspend fun fetch(place: Place): Forecast {
        val response = networkCall { api.forecast(place.latitude, place.longitude) }
        val fetchedAt = Instant.now()
        store.writeCachedForecast(place.id, CachedForecast(fetchedAt.toEpochMilli(), response))
        return response.toDomain(place, fetchedAt)
    }
}
