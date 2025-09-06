package com.umain.transport.sites.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SiteDto(
    @SerialName("SiteId")
    val siteId: Int,
    @SerialName("SiteName")
    val siteName: String,
    @SerialName("X")
    val x: String,
    @SerialName("Y")
    val y: String,
)
