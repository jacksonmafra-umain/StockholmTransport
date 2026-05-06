package com.umain.transport.realtime.domain.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * One frame of "where is this trip right now" — the renderable snapshot a
 * UI layer (Compose, React, Swift, …) can bind to without knowing anything
 * about WebSockets, Mongo, or the Trafiklab schema.
 *
 * Only contains String + Station — JS-friendly types that survive
 * `JSON.stringify` cleanly without leaking Kotlin internal field names.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
data class TripDisplayInfo(
    val currentStation: Station,
    val nextStations: List<Station>,
    val finalDestination: Station,
)
