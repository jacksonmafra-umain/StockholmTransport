package com.umain.transport.realtime.data.remote

import com.umain.transport.core.logging.AppLogger
import com.umain.transport.realtime.RealtimeConfig
import com.umain.transport.realtime.data.model.WebSocketMessageDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * Live trip stream. Connects to the simulator's `/updates/{tripId}`
 * WebSocket and emits one [WebSocketMessageDto] per tick. Uses the same
 * core HttpClient the rest of the library uses (Ktor Logging + the JSON
 * config in [com.umain.transport.core.network.createHttpClient]).
 *
 * Wire-level errors propagate as Flow exceptions; the repository wraps
 * them in DataResult.Error before they reach the ViewModel.
 */
internal class TripUpdateDataSource(
    private val httpClient: HttpClient,
    private val config: RealtimeConfig,
) {
    private val tag = "TripUpdateDataSource"
    private val json = Json { ignoreUnknownKeys = true }

    fun connect(tripId: String): Flow<WebSocketMessageDto> = flow {
        AppLogger.i(
            tag,
            "Opening WebSocket → ${if (config.wsSecure) "wss" else "ws"}://${config.wsHost}:${config.wsPort}/updates/$tripId",
        )
        httpClient.webSocket(
            host = config.wsHost,
            port = config.wsPort,
            path = "/updates/$tripId",
        ) {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    try {
                        emit(json.decodeFromString<WebSocketMessageDto>(text))
                    } catch (e: Exception) {
                        AppLogger.e(tag, "Failed to parse WS frame: ${text.take(200)}", e)
                    }
                }
            }
        }
    }.catch { e ->
        AppLogger.e(tag, "WebSocket connection failed for tripId=$tripId", e)
        throw e
    }
}
