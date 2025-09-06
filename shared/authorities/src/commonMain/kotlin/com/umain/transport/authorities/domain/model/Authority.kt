package com.umain.transport.authorities.domain.model

data class Authority(
    val id: Int,
    val name: String,
    val formalName: String?,
    val city: String?,
    val country: String?
)