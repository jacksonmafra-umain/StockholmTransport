package com.umain.transport.departures.domain.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
data class Departure(
    val lineDesignation: String,
    val destination: String,
    val displayTime: String,
    val transportMode: String,
)
