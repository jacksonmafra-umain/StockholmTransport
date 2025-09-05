package com.umain.transport.departures.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeparturesResponseDto(
    val departures: List<DepartureDto> = emptyList()
)

@Serializable
data class DepartureDto(
    val destination: String,
    val direction: String,
    val scheduled: String,
    val expected: String? = null,
    val display: String,
    val line: LineInfoDto,
)

@Serializable
data class LineInfoDto(
    val id: Int,
    val designation: String,
    @SerialName("transport_mode")
    val transportMode: String
)