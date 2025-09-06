package com.umain.transport.authorities.data.repository

import com.umain.transport.authorities.data.model.AuthorityDto
import com.umain.transport.authorities.domain.model.Authority
import com.umain.transport.authorities.domain.repository.AuthoritiesRepository
import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.core.logging.AppLogger
import com.umain.transport.core.network.API_BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.utils.io.errors.IOException

class AuthoritiesRepositoryImpl(private val httpClient: HttpClient) : AuthoritiesRepository {
    private val tag = "AuthoritiesRepository"

    override suspend fun getAllAuthorities(): DataResult<List<Authority>> {
        return try {
            val response = httpClient.get("$API_BASE_URL/transport-authorities").body<List<AuthorityDto>>()
            DataResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            AppLogger.e(tag, "Failed to fetch all authorities", e)
            val networkError = when (e) {
                is HttpRequestTimeoutException -> NetworkError.Timeout
                is IOException -> NetworkError.NoInternet
                else -> NetworkError.Unknown(e.message ?: "An unknown error occurred")
            }
            DataResult.Error(networkError)
        }
    }

    private fun AuthorityDto.toDomain(): Authority {
        return Authority(
            id = this.id,
            name = this.name
        )
    }
}