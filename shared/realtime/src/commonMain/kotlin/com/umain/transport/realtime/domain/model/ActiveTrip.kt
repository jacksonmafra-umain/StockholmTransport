package com.umain.transport.realtime.domain.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Lightweight summary the trip-selection screen lists — a tripId + enough
 * line metadata to colour the row and route the user into the live screen.
 *
 * `transportMode` is a String (not the lines module's TransportMode enum)
 * so JSON.stringify produces clean output for browser/Node consumers — same
 * trick used in `Line.transportMode`.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
data class ActiveTrip(
    val tripId: String,
    val lineId: String,
    val lineNumber: String,
    val transportMode: String,
)
