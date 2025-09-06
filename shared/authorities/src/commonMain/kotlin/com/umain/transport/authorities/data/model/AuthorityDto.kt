package com.umain.transport.authorities.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthorityDto(
    val id: Int,
    val name: String,
)
