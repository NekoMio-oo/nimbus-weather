package com.example.nimbus.domain.usecase

import com.example.nimbus.domain.model.Forecast
import com.example.nimbus.domain.model.Place
import com.example.nimbus.domain.repository.WeatherRepository
import java.time.Duration
import java.time.Instant

/**
 * The forecast for a place: a fresh cached copy when there is one, otherwise a network fetch. The result
 * carries the failure instead of throwing, so a screen can decide what to do with the copy it already has.
 */
class GetForecastUseCase(
    private val weather: WeatherRepository,
    private val maxAge: Duration = Duration.ofMinutes(15),
    private val clock: () -> Instant = { Instant.now() },
) {
    suspend operator fun invoke(place: Place, forceRefresh: Boolean = false): Result<Forecast> {
        if (!forceRefresh) {
            val cached = weather.cached(place)
            if (cached != null && cached.isFresh(clock(), maxAge)) return Result.success(cached)
        }
        return runCatching { weather.fetch(place) }
    }

    /** Whatever is stored for [place], fresh or not, for showing while a fetch is in flight. */
    fun cached(place: Place): Forecast? = weather.cached(place)
}
