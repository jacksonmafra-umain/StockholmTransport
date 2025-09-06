package com.umain.transport.authorities.domain.repository

import com.umain.transport.authorities.domain.model.Authority
import com.umain.transport.core.data.DataResult

interface AuthoritiesRepository {
    suspend fun getAllAuthorities(): DataResult<List<Authority>>
}
