package com.umain.transport.sites.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SiteDto(
    val id: Int,
    // The SL API omits gid/lat/lon for some abstract sites (e.g. $[2968]).
    // A *missing* field needs a default or kotlinx.serialization fails the
    // whole list, so these are optional; the mapper defaults coords to 0.0.
    val gid: Long? = null,
    val name: String,
    val lat: Double? = null,
    val lon: Double? = null,
    @SerialName("stop_areas")
    val stopAreas: List<Int> = emptyList()
)