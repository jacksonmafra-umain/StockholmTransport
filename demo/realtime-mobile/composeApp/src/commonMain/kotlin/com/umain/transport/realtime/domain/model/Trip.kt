package com.umain.transport.realtime.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    @SerialName("_id") val id: String,
    val line: String,
    val startTime: String,
    val status: String
)