package com.umain.transport.lines.domain.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
data class Line(
    val id: Int,
    val name: String,
    val designation: String,
    val transportMode: TransportMode,
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
