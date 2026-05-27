package com.umain.transport.departures.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeparturesResponseDto(
    val departures: List<DepartureDto> = emptyList(),
    @SerialName("stop_deviations")
    val stopDeviations: List<StopDeviationDto> = emptyList()
)

// The Departure domain model only reads destination, display, and line
// (designation + transport_mode). Everything else is optional — the SL API
// omits various fields per record, and a missing field fails the whole list
// unless it has a default.
@Serializable
data class DepartureDto(
    val destination: String,
    val direction: String? = null,
    val scheduled: String? = null,
    val expected: String? = null,
    val display: String,
    val line: LineInfoDto,
    @SerialName("stop_area")
    val stopArea: StopAreaDto? = null,
    @SerialName("stop_point")
    val stopPoint: StopPointDto? = null,
    val deviations: List<DeviationDto> = emptyList()
)

@Serializable
data class LineInfoDto(
    val id: Int? = null,
    val designation: String,
    @SerialName("transport_mode")
    val transportMode: String,
    @SerialName("group_of_lines")
    val groupOfLines: String? = null
)

@Serializable
data class StopAreaDto(
    val id: Int? = null,
    val name: String? = null,
    val type: String? = null
)

@Serializable
data class StopPointDto(
    val id: Int,
    val gid: Long? = null,
    val name: String,
    val designation: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    @SerialName("stop_area")
    val stopArea: StopAreaDto? = null
)

// Deviation metadata is advisory and frequently partial in the SL feed; none
// of it reaches the domain, so every field is optional.
@Serializable
data class DeviationDto(
    @SerialName("importance_level")
    val importanceLevel: Int? = null,
    val consequence: String? = null,
    val message: String? = null
)

@Serializable
data class StopDeviationDto(
    @SerialName("importance_level")
    val importanceLevel: Int? = null,
    val consequence: String? = null,
    val message: String? = null
)