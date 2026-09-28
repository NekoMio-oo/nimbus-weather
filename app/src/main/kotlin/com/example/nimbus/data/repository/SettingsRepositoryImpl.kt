package com.example.nimbus.data.repository

import com.example.nimbus.data.local.LocalStore
import com.example.nimbus.domain.model.UnitSystem
import com.example.nimbus.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class SettingsRepositoryImpl(private val store: LocalStore) : SettingsRepository {

    override val unitSystem: Flow<UnitSystem> = store.unitSystemName.map { name ->
        name?.let { stored -> UnitSystem.entries.firstOrNull { it.name == stored } } ?: defaultForLocale()
    }

    override suspend fun setUnitSystem(unitSystem: UnitSystem) {
        store.writeUnitSystemName(unitSystem.name)
    }

    /** Fahrenheit where it is the everyday scale, Celsius everywhere else. */
    private fun defaultForLocale(): UnitSystem =
        if (Locale.getDefault().country in IMPERIAL_COUNTRIES) UnitSystem.IMPERIAL else UnitSystem.METRIC

    private companion object {
        val IMPERIAL_COUNTRIES = setOf("US", "BS", "BZ", "KY", "PW", "FM", "MH", "LR")
    }
}
