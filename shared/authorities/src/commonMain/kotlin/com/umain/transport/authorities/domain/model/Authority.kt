package com.umain.transport.authorities.domain.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
data class Authority(
    val id: Int,
    val name: String,
    val formalName: String?,
    val city: String?,
    val country: String?
)