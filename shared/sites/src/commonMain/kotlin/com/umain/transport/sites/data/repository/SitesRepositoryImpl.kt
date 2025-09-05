package com.umain.transport.sites.data.repository

import com.umain.transport.core.network.API_BASE_URL
import com.umain.transport.sites.data.model.SiteDto
import com.umain.transport.sites.domain.model.Site
import com.umain.transport.sites.domain.repository.SitesRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class SitesRepositoryImpl(private val httpClient: HttpClient) : SitesRepository {
    override suspend fun getAllSites(): Result<List<Site>> {
        return try {
            val response = httpClient.get("$API_BASE_URL/sites").body<List<SiteDto>>()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun SiteDto.toDomain(): Site {
        return Site(
            id = this.siteId,
            name = this.siteName
        )
    }
}