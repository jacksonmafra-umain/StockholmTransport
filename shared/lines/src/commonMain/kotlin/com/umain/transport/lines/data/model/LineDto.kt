package com.umain.transport.lines.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinesResponse(
    val metro: List<LineDto> = emptyList(),
    val tram: List<LineDto> = emptyList(),
    val train: List<LineDto> = emptyList(),
    val bus: List<LineDto> = emptyList(),
    val ship: List<LineDto> = emptyList(),
    val ferry: List<LineDto> = emptyList(),
    val taxi: List<LineDto> = emptyList(),
)

@Serializable
data class LineDto(
    val id: Int,
    val gid: Long,
    val name: String,
    val designation: String,
    @SerialName("transport_mode")
    val transportMode: String,
    @SerialName("group_of_lines")
    val groupOfLines: String? = null,
    @SerialName("transport_authority")
    val transportAuthority: TransportAuthorityDto,
)

@Serializable
data class TransportAuthorityDto(
    val id: Int,
    val name: String,
)
