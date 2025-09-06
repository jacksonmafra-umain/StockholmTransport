package com.umain.transport.stoppoints.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StopPointDto(
    @SerialName("StopPointNumber")
    val stopPointNumber: Int,
    @SerialName("StopPointName")
    val stopPointName: String,
    @SerialName("StopAreaNumber")
    val stopAreaNumber: Int,
    @SerialName("LocationNorthing")
    val locationNorthing: Double,
    @SerialName("LocationEasting")
    val locationEasting: Double,
    @SerialName("ZoneShortName")
    val zoneShortName: String,
)
