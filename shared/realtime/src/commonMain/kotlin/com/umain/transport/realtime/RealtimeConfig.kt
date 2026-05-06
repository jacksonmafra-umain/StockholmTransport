package com.umain.transport.realtime

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Where to find the realtime simulator at runtime.
 *
 * Held as a separate config (rather than baked into the library's
 * BuildConfig) because the simulator's host/port commonly differ between
 * dev (`localhost:3001`), CI (`docker compose up`'s service name), and
 * the talk-day setup (`./sl start` rewrites this to the ngrok URL).
 *
 * Consumers pass an instance of this when initialising Koin via
 * [com.umain.transport.di.initKoin].
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
data class RealtimeConfig(
    /** Base URL for HTTP REST calls (e.g. `http://localhost:3001`). */
    val httpBaseUrl: String,
    /** Hostname for the WebSocket (e.g. `localhost`). */
    val wsHost: String,
    /** Port for the WebSocket (e.g. `3001`). */
    val wsPort: Int,
    /**
     * `true` when the WebSocket is served over `wss://` (TLS); `false` for
     * plain `ws://`. Defaults to `false` because the simulator runs without
     * TLS in dev. Behind ngrok, set this to `true`.
     */
    val wsSecure: Boolean = false,
) {
    companion object {
        /** The default dev profile — `docker compose up`'s realtime-api service. */
        @JsExport.Ignore
        fun localhost(): RealtimeConfig = RealtimeConfig(
            httpBaseUrl = "http://localhost:3001",
            wsHost = "localhost",
            wsPort = 3001,
            wsSecure = false,
        )
    }
}
