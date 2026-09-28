package com.example.nimbus.domain.model

/**
 * Why a forecast or a search could not be loaded, in terms the UI can explain. The data layer translates
 * transport failures into one of these so nothing above it needs to know about sockets or HTTP.
 */
sealed class ForecastException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** No network route to the service. */
    class Offline(cause: Throwable? = null) : ForecastException("offline", cause)

    /** The service was reachable but did not answer in time. */
    class Timeout(cause: Throwable? = null) : ForecastException("timeout", cause)

    /** The service answered with an error status. */
    class Server(val statusCode: Int, cause: Throwable? = null) : ForecastException("server $statusCode", cause)

    /** Anything else, including a response the app could not parse. */
    class Unexpected(cause: Throwable? = null) : ForecastException("unexpected", cause)
}
