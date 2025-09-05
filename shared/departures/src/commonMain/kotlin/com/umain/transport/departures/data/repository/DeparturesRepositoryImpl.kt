package com.umain.transport.departures.data.repository

import com.umain.transport.core.network.API_BASE_URL
import com.umain.transport.departures.data.model.DepartureDto
import com.umain.transport.departures.data.model.DeparturesResponseDto
import com.umain.transport.departures.domain.model.Departure
import com.umain.transport.departures.domain.repository.DeparturesRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class DeparturesRepositoryImpl(private val httpClient: HttpClient) : DeparturesRepository {
    override suspend fun getDeparturesForSite(siteId: Int): Result<List<Departure>> {
        return try {
            val response = httpClient.get("$API_BASE_URL/sites/$siteId/departures") {
                parameter("forecast", 60)
            }.body<DeparturesResponseDto>()
            Result.success(response.departures.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
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