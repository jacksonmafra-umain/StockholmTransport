package com.umain.transport.sites.data.repository

import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.core.logging.AppLogger
import com.umain.transport.core.network.API_BASE_URL
import com.umain.transport.sites.data.model.SiteDto
import com.umain.transport.sites.domain.model.Site
import com.umain.transport.sites.domain.repository.SitesRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.utils.io.errors.IOException

class SitesRepositoryImpl(private val httpClient: HttpClient) : SitesRepository {
    private val tag = "SitesRepository"

    override suspend fun getAllSites(): DataResult<List<Site>> {
        return try {
            val response = httpClient.get("$API_BASE_URL/sites").body<List<SiteDto>>()
            DataResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            AppLogger.e(tag, "Failed to fetch all sites", e)
            val networkError = when (e) {
                is HttpRequestTimeoutException -> NetworkError.Timeout
                is IOException -> NetworkError.NoInternet
                else -> NetworkError.Unknown(e.message ?: "An unknown error occurred")
            }
            DataResult.Error(networkError)
        }
    }

    private fun SiteDto.toDomain(): Site {
        return Site(
            id = this.siteId,
            name = this.siteName
        )
    }
}