package com.umain.transport.stoppoints.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StopPointDto(
    val id: Int,
    val gid: Long,
    val name: String,
    val designation: String? = null,
    val type: String,
    val lat: Double,
    val lon: Double,
    @SerialName("stop_area")
    val stopArea: StopAreaDto,
    @SerialName("transport_authority")
    val transportAuthority: TransportAuthorityDto
)

@Serializable
data class StopAreaDto(
    val id: Int,
    val name: String,
    val type: String
)

@Serializable
data class TransportAuthorityDto(
    val id: Int,
    val name: String
)