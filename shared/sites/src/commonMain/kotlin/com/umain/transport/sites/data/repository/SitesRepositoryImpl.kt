package com.umain.transport.sites.data.repository

import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.core.logging.AppLogger
import com.umain.transport.sites.data.model.SiteDto
import com.umain.transport.sites.domain.model.Site
import com.umain.transport.sites.domain.repository.SitesRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import kotlinx.io.IOException

class SitesRepositoryImpl(private val httpClient: HttpClient) : SitesRepository {
    private val tag = "SitesRepository"

    override suspend fun getAllSites(): DataResult<List<Site>> {
        return try {
            val response = httpClient.get("v1/sites").body<List<SiteDto>>()
            DataResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            AppLogger.e(tag, "Failed to fetch all sites", e)
            val networkError = when (e) {
                is HttpRequestTimeoutException -> NetworkError.Timeout
                is IOException -> NetworkError.NoInternet
                is ResponseException -> when (e.response.status.value) {
                    404 -> NetworkError.NotFound
                    in 500..599 -> NetworkError.ServerError
                    else -> NetworkError.Unknown("HTTP Error: ${e.response.status.value}")
                }
                else -> NetworkError.Unknown(e.message ?: "An unknown error occurred")
            }
            DataResult.Error(networkError)
        }
    }

    private fun SiteDto.toDomain(): Site {
        return Site(
            id = this.id,
            name = this.name,
            latitude = this.lat,
            longitude = this.lon
        )
    }
}