package com.umain.transport.stoppoints.domain.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
data class StopPoint(
    val id: Int,
    val name: String,
    val type: String, // Ex: "PLATFORM"
    val stopAreaName: String,
    val authorityName: String,
    val latitude: Double,
    val longitude: Double
)