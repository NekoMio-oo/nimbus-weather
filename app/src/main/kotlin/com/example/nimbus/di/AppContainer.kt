package com.example.nimbus.di

import android.content.Context
import com.example.nimbus.data.local.LocalStore
import com.example.nimbus.data.remote.GeocodingApi
import com.example.nimbus.data.remote.HttpClient
import com.example.nimbus.data.remote.OpenMeteoGeocodingApi
import com.example.nimbus.data.remote.UapiWeatherApi
import com.example.nimbus.data.remote.WeatherApi
import com.example.nimbus.data.repository.PlacesRepositoryImpl
import com.example.nimbus.data.repository.SettingsRepositoryImpl
import com.example.nimbus.data.repository.WeatherRepositoryImpl
import com.example.nimbus.domain.repository.PlacesRepository
import com.example.nimbus.domain.repository.SettingsRepository
import com.example.nimbus.domain.repository.WeatherRepository
import com.example.nimbus.domain.usecase.GetForecastUseCase
import com.example.nimbus.domain.usecase.ObserveSavedPlacesUseCase
import com.example.nimbus.domain.usecase.ObserveUnitSystemUseCase
import com.example.nimbus.domain.usecase.RemovePlaceUseCase
import com.example.nimbus.domain.usecase.SavePlaceUseCase
import com.example.nimbus.domain.usecase.SearchPlacesUseCase
import com.example.nimbus.domain.usecase.SetUnitSystemUseCase
import kotlinx.serialization.json.Json

/**
 * Hand-wired dependency graph, built once by the Application. Each layer only sees the interface below
 * it; swapping the weather service or the storage means changing one line here. A DI framework such as
 * Hilt would replace this class and nothing else.
 */
class AppContainer(context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // Read on each call so a per-app language change is picked up. The geocoder takes any language and
    // falls back to English; the weather service only knows Chinese and English.
    private val appLanguage: () -> String = { context.resources.configuration.locales[0].language }

    private val geocodingApi: GeocodingApi = OpenMeteoGeocodingApi(
        http = HttpClient(),
        json = json,
        language = appLanguage,
    )

    private val weatherApi: WeatherApi = UapiWeatherApi(
        http = HttpClient(),
        json = json,
        apiKey = UAPI_WEATHER_KEY,
    )

    private val store = LocalStore(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
        json,
    )

    val placesRepository: PlacesRepository = PlacesRepositoryImpl(geocodingApi, store)
    val weatherRepository: WeatherRepository = WeatherRepositoryImpl(
        api = weatherApi,
        store = store,
        language = { if (appLanguage() == "zh") "zh" else "en" },
    )
    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(store)

    val observeSavedPlaces = ObserveSavedPlacesUseCase(placesRepository)
    val searchPlaces = SearchPlacesUseCase(placesRepository)
    val savePlace = SavePlaceUseCase(placesRepository)
    val removePlace = RemovePlaceUseCase(placesRepository)
    val getForecast = GetForecastUseCase(weatherRepository)
    val observeUnitSystem = ObserveUnitSystemUseCase(settingsRepository)
    val setUnitSystem = SetUnitSystemUseCase(settingsRepository)

    private companion object {
        const val PREFS_NAME = "nimbus"

        /** UApiPro's key for the `/misc/weather` endpoint, sent as the `key` query parameter. */
        const val UAPI_WEATHER_KEY = "uapi-1s7voejsD_8357IEPHBlYxvFPJDQuzY6eGEQ94Qh"
    }
}
