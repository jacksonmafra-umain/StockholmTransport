package com.umain.transport.core.network

import com.umain.transport.config.BuildConfig
import com.umain.transport.core.logging.AppLogger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val API_BASE_URL = BuildConfig.API_BASE_URL

class KtorLogger : Logger {
    override fun log(message: String) {
        AppLogger.d("KtorHttpClient", message)
    }
}

fun createHttpClient() = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            // A present-but-null value on a non-null property (with a default)
            // degrades to the default instead of failing the whole response.
            // Pairs with the nullable DTO fields for missing keys.
            coerceInputValues = true
        })
    }

    install(Logging) {
        logger = KtorLogger()
        level = LogLevel.ALL
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 15000
        connectTimeoutMillis = 15000
        socketTimeoutMillis = 15000
    }

    // Realtime feature opens `/updates/{tripId}` over WS — see
    // [com.umain.transport.realtime.data.remote.TripUpdateDataSource].
    install(WebSockets)

    defaultRequest {
        url(API_BASE_URL)
        url {
            parameters.append("key", BuildConfig.API_KEY)
        }
        contentType(ContentType.Application.Json)
    }

    expectSuccess = true
}