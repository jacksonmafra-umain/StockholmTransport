package com.umain.transport.departures.data.repository

import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.core.logging.AppLogger
import com.umain.transport.core.network.API_BASE_URL
import com.umain.transport.departures.data.model.DepartureDto
import com.umain.transport.departures.data.model.DeparturesResponseDto
import com.umain.transport.departures.domain.model.Departure
import com.umain.transport.departures.domain.repository.DeparturesRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.utils.io.errors.IOException

class DeparturesRepositoryImpl(private val httpClient: HttpClient) : DeparturesRepository {
    private val tag = "DeparturesRepository"

    override suspend fun getDeparturesForSite(siteId: Int): DataResult<List<Departure>> {
        return try {
            val response = httpClient.get("$API_BASE_URL/sites/$siteId/departures") {
                parameter("forecast", 60)
            }.body<DeparturesResponseDto>()
            DataResult.Success(response.departures.map { it.toDomain() })
        } catch (e: Exception) {
            AppLogger.e(tag, "Failed to fetch departures for siteId: $siteId", e)
            val networkError = when (e) {
                is HttpRequestTimeoutException -> NetworkError.Timeout
                is IOException -> NetworkError.NoInternet
                else -> NetworkError.Unknown(e.message ?: "An unknown error occurred")
            }
            DataResult.Error(networkError)
        }
    }

    private fun DepartureDto.toDomain(): Departure {
        return Departure(
            lineDesignation = this.line.designation,
            destination = this.destination,
            displayTime = this.display,
            transportMode = this.line.transportMode
        )
    }
}