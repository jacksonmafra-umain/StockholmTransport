package com.umain.transport.sites.domain.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
data class Site(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double
)