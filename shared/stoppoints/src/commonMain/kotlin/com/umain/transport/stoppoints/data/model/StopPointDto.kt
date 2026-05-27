package com.umain.transport.stoppoints.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Optional fields mirror the SL API's habit of omitting gid/lat/lon (and
// occasionally the nested objects) on incomplete records. A missing field needs
// a default or the whole list fails to deserialise; the mapper falls back to
// 0.0 coords and "" names.
@Serializable
data class StopPointDto(
    val id: Int,
    val gid: Long? = null,
    val name: String,
    val designation: String? = null,
    val type: String,
    val lat: Double? = null,
    val lon: Double? = null,
    @SerialName("stop_area")
    val stopArea: StopAreaDto? = null,
    @SerialName("transport_authority")
    val transportAuthority: TransportAuthorityDto? = null
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