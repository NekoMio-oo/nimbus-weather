package com.example.nimbus.data.local

import android.content.SharedPreferences
import com.example.nimbus.data.remote.CachedForecast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** A saved place as written to disk. Kept apart from the domain model so the file format can outlive it. */
@Serializable
data class StoredPlace(
    val id: Long,
    val name: String,
    val region: String? = null,
    val country: String = "",
    val latitude: Double,
    val longitude: Double,
    val timeZone: String,
)

/**
 * Everything Nimbus persists, in one [SharedPreferences] file as JSON strings: the saved places, the unit
 * preference, and the last forecast per place. The data is a few kilobytes, so a database would be more
 * ceremony than storage; the state flows let repositories expose changes without polling.
 */
class LocalStore(
    private val prefs: SharedPreferences,
    private val json: Json,
) {
    private val placesSerializer = ListSerializer(StoredPlace.serializer())

    private val placesState = MutableStateFlow(readPlaces())
    val places: StateFlow<List<StoredPlace>> = placesState.asStateFlow()

    private val unitSystemState = MutableStateFlow(prefs.getString(KEY_UNIT_SYSTEM, null))
    /** The stored unit system's name, or null when the user has never chosen. */
    val unitSystemName: StateFlow<String?> = unitSystemState.asStateFlow()

    fun writePlaces(places: List<StoredPlace>) {
        placesState.value = places
        prefs.edit().putString(KEY_PLACES, json.encodeToString(placesSerializer, places)).apply()
    }

    fun writeUnitSystemName(name: String) {
        unitSystemState.value = name
        prefs.edit().putString(KEY_UNIT_SYSTEM, name).apply()
    }

    fun cachedForecast(placeId: Long): CachedForecast? {
        val raw = prefs.getString(forecastKey(placeId), null) ?: return null
        return runCatching { json.decodeFromString(CachedForecast.serializer(), raw) }.getOrNull()
    }

    fun writeCachedForecast(placeId: Long, cached: CachedForecast) {
        prefs.edit()
            .putString(forecastKey(placeId), json.encodeToString(CachedForecast.serializer(), cached))
            .apply()
    }

    fun removeCachedForecast(placeId: Long) {
        prefs.edit().remove(forecastKey(placeId)).apply()
    }

    private fun readPlaces(): List<StoredPlace> {
        val raw = prefs.getString(KEY_PLACES, null) ?: return emptyList()
        return runCatching { json.decodeFromString(placesSerializer, raw) }.getOrDefault(emptyList())
    }

    private fun forecastKey(placeId: Long) = "$KEY_FORECAST_PREFIX$placeId"

    private companion object {
        const val KEY_PLACES = "places"
        const val KEY_UNIT_SYSTEM = "unit_system"
        const val KEY_FORECAST_PREFIX = "forecast:"
    }
}
