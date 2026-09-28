package com.example.nimbus.di

import android.content.Context
import com.example.nimbus.data.local.LocalStore
import com.example.nimbus.data.remote.HttpClient
import com.example.nimbus.data.remote.OpenMeteoApi
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

    private val api: WeatherApi = OpenMeteoApi(HttpClient(), json)

    private val store = LocalStore(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
        json,
    )

    val placesRepository: PlacesRepository = PlacesRepositoryImpl(api, store)
    val weatherRepository: WeatherRepository = WeatherRepositoryImpl(api, store)
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
    }
}
