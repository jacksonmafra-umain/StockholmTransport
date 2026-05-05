package com.umain.transport.lines.domain.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
data class Line(
    val id: Int,
    val name: String,
    val designation: String,
    // String, not TransportMode, so JS consumers see "METRO" instead of
    // Kotlin's internal enum representation (`{z_1: "METRO", a1_1: 0}`)
    // when the state is serialized via JSON.stringify.
    val transportMode: String,
    val authority: String,
)

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class TransportMode {
    METRO,
    TRAM,
    TRAIN,
    BUS,
    SHIP,
    FERRY,
    TAXI,
    UNKNOWN,
}
