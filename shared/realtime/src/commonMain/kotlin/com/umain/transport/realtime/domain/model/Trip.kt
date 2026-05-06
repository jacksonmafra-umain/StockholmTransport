package com.umain.transport.realtime.domain.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * The Trip resource as returned by `POST /trip/start/{lineId}` on the
 * realtime simulator. `id`, `lineId`, `startTime`, and `status` are the
 * minimum fields a consumer needs to track the trip's lifecycle.
 *
 * `status` is a String for the same JS-friendliness reason as
 * ActiveTrip.transportMode — values are typically "ACTIVE" or "STOPPED".
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
data class Trip(
    val id: String,
    val lineId: String,
    val startTime: String,
    val status: String,
)
