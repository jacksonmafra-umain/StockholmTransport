package com.umain.transport.realtime.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ActiveTrip(
    val tripId: String,
    val lineId: String,
    val lineNumber: String,
    val transportMode: String
)