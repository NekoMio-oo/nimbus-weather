package com.example.nimbus.data.repository

import com.example.nimbus.data.remote.HttpException
import com.example.nimbus.domain.model.ForecastException
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Run a network call and translate any failure into a [ForecastException]. Cancellation is passed through
 * untouched so a coroutine that was cancelled is never reported as an error.
 */
internal suspend inline fun <T> networkCall(block: () -> T): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: ForecastException) {
    throw e
} catch (e: HttpException) {
    throw ForecastException.Server(e.statusCode, e)
} catch (e: SocketTimeoutException) {
    throw ForecastException.Timeout(e)
} catch (e: UnknownHostException) {
    throw ForecastException.Offline(e)
} catch (e: IOException) {
    throw ForecastException.Offline(e)
} catch (e: Exception) {
    throw ForecastException.Unexpected(e)
}
