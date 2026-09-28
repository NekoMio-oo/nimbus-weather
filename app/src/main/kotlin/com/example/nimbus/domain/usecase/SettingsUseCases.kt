package com.example.nimbus.domain.usecase

import com.example.nimbus.domain.model.UnitSystem
import com.example.nimbus.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveUnitSystemUseCase(private val settings: SettingsRepository) {
    operator fun invoke(): Flow<UnitSystem> = settings.unitSystem
}

class SetUnitSystemUseCase(private val settings: SettingsRepository) {
    suspend operator fun invoke(unitSystem: UnitSystem) = settings.setUnitSystem(unitSystem)
}
