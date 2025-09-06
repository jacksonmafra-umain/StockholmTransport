package com.umain.transport.authorities.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthorityDto(
    val id: Int,
    val gid: Long,
    val name: String,
    @SerialName("formal_name")
    val formalName: String? = null,
    val city: String? = null,
    val country: String? = null
)