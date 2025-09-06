package com.umain.transport.departures.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeparturesResponseDto(
    val departures: List<DepartureDto> = emptyList(),
    @SerialName("stop_deviations")
    val stopDeviations: List<StopDeviationDto> = emptyList()
)

@Serializable
data class DepartureDto(
    val destination: String,
    val direction: String,
    val scheduled: String,
    val expected: String? = null,
    val display: String,
    val line: LineInfoDto,
    @SerialName("stop_area")
    val stopArea: StopAreaDto,
    @SerialName("stop_point")
    val stopPoint: StopPointDto,
    val deviations: List<DeviationDto> = emptyList()
)

@Serializable
data class LineInfoDto(
    val id: Int,
    val designation: String,
    @SerialName("transport_mode")
    val transportMode: String,
    @SerialName("group_of_lines")
    val groupOfLines: String? = null
)

@Serializable
data class StopAreaDto(
    val id: Int,
    val name: String,
    val type: String
)

@Serializable
data class StopPointDto(
    val id: Int,
    val gid: Long,
    val name: String,
    val designation: String? = null,
    val lat: Double,
    val lon: Double,
    @SerialName("stop_area")
    val stopArea: StopAreaDto
)

@Serializable
data class DeviationDto(
    @SerialName("importance_level")
    val importanceLevel: Int,
    val consequence: String,
    val message: String
)

@Serializable
data class StopDeviationDto(
    @SerialName("importance_level")
    val importanceLevel: Int,
    val consequence: String,
    val message: String
)