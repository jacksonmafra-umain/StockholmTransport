package com.umain.transport.realtime.domain.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * A point on a transport line where vehicles stop. Pure domain — no
 * persistence concerns, no platform types. Reused by the static SDK feature
 * modules' Site / StopPoint references and the realtime simulator's trip
 * payloads.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
data class Station(
    val name: String,
)
