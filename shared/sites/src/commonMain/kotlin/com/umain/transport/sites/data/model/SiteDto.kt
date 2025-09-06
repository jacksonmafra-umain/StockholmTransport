package com.umain.transport.sites.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SiteDto(
    val id: Int,
    val gid: Long,
    val name: String,
    val lat: Double,
    val lon: Double,
    @SerialName("stop_areas")
    val stopAreas: List<Int> = emptyList()
)