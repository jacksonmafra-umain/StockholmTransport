package com.umain.transport.authorities.domain.repository

import com.umain.transport.authorities.domain.model.Authority

interface AuthoritiesRepository {
    suspend fun getAllAuthorities(): Result<List<Authority>>
}