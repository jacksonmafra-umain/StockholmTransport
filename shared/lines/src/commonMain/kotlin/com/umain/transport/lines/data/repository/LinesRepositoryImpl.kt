package com.umain.transport.lines.data.repository

import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.core.logging.AppLogger
import com.umain.transport.core.network.API_BASE_URL
import com.umain.transport.lines.data.model.LineDto
import com.umain.transport.lines.data.model.LinesResponse
import com.umain.transport.lines.domain.model.Line
import com.umain.transport.lines.domain.model.TransportMode
import com.umain.transport.lines.domain.repository.LinesRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import kotlinx.io.IOException

class LinesRepositoryImpl(
    private val httpClient: HttpClient,
) : LinesRepository {
    private val tag = "LinesRepository"

    override suspend fun getAllLines(): DataResult<Map<TransportMode, List<Line>>> {
        return try {
            val response = httpClient.get("lines") {
                parameter("transport_authority_id", 1)
            }.body<List<LinesResponse>>().firstOrNull()

            if (response == null) {
                DataResult.Success(emptyMap())
            } else {
                DataResult.Success(mapResponseToDomain(response))
            }
        } catch (e: Exception) {
            AppLogger.e(tag, "Failed to fetch all lines", e)
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
    private fun mapResponseToDomain(response: LinesResponse): Map<TransportMode, List<Line>> =
        mapOf(
            TransportMode.METRO to response.metro.map { it.toDomain() },
            TransportMode.TRAM to response.tram.map { it.toDomain() },
            TransportMode.TRAIN to response.train.map { it.toDomain() },
            TransportMode.BUS to response.bus.map { it.toDomain() },
            TransportMode.SHIP to response.ship.map { it.toDomain() },
            TransportMode.FERRY to response.ferry.map { it.toDomain() },
            TransportMode.TAXI to response.taxi.map { it.toDomain() },
        ).filter { it.value.isNotEmpty() }

    private fun LineDto.toDomain(): Line {
        val mode =
            when (transportMode.uppercase()) {
                "METRO" -> TransportMode.METRO
                "TRAM" -> TransportMode.TRAM
                "TRAIN" -> TransportMode.TRAIN
                "BUS" -> TransportMode.BUS
                "SHIP" -> TransportMode.SHIP
                "FERRY" -> TransportMode.FERRY
                "TAXI" -> TransportMode.TAXI
                else -> TransportMode.UNKNOWN
            }
        return Line(
            id = this.id,
            name = this.name,
            designation = this.designation,
            transportMode = mode,
            authority = this.transportAuthority.name,
        )
    }
}
