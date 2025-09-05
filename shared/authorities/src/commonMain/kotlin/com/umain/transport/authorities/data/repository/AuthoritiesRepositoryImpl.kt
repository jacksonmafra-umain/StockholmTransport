package com.umain.transport.authorities.data.repository

import com.umain.transport.authorities.data.model.AuthorityDto
import com.umain.transport.authorities.domain.model.Authority
import com.umain.transport.authorities.domain.repository.AuthoritiesRepository
import com.umain.transport.core.network.API_BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class AuthoritiesRepositoryImpl(private val httpClient: HttpClient) : AuthoritiesRepository {
    override suspend fun getAllAuthorities(): Result<List<Authority>> {
        return try {
            val response = httpClient.get("$API_BASE_URL/transport-authorities").body<List<AuthorityDto>>()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun AuthorityDto.toDomain(): Authority {
        return Authority(
            id = this.id,
            name = this.name
        )
    }
}