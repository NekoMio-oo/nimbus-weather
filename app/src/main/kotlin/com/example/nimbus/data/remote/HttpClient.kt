package com.example.nimbus.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * A minimal HTTP GET on top of the platform's [HttpURLConnection]. Nimbus only ever fetches two small JSON
 * documents, so this avoids pulling a networking library into the build; swap in OkHttp or Ktor here if a
 * project grows past that, and nothing above [WeatherApi] changes.
 */
class HttpClient(
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 15_000,
) {
    suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", USER_AGENT)
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
            if (status !in 200..299) throw HttpException(status, body)
            body
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val USER_AGENT = "Nimbus/1.0 (CodeAssist sample)"
    }
}

/** A non-2xx answer. [body] is whatever the server sent, for logging. */
class HttpException(val statusCode: Int, val body: String) : IOException("HTTP $statusCode")
