package com.umain.transport.lines.data.repository

import com.umain.transport.core.network.API_BASE_URL
import com.umain.transport.lines.data.model.LineDto
import com.umain.transport.lines.data.model.LinesResponse
import com.umain.transport.lines.domain.model.Line
import com.umain.transport.lines.domain.model.TransportMode
import com.umain.transport.lines.domain.repository.LinesRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class LinesRepositoryImpl(private val httpClient: HttpClient) : LinesRepository {
    override suspend fun getAllLines(): Result<Map<TransportMode, List<Line>>> {
        return try {
            val response = httpClient.get("$API_BASE_URL/lines").body<List<LinesResponse>>().firstOrNull()
            if (response == null) {
                Result.success(emptyMap())
            } else {
                Result.success(mapResponseToDomain(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapResponseToDomain(response: LinesResponse): Map<TransportMode, List<Line>> {
        return mapOf(
            TransportMode.METRO to response.metro.map { it.toDomain() },
            TransportMode.TRAM to response.tram.map { it.toDomain() },
            TransportMode.TRAIN to response.train.map { it.toDomain() },
            TransportMode.BUS to response.bus.map { it.toDomain() },
            TransportMode.SHIP to response.ship.map { it.toDomain() },
            TransportMode.FERRY to response.ferry.map { it.toDomain() },
            TransportMode.TAXI to response.taxi.map { it.toDomain() }
        ).filter { it.value.isNotEmpty() }
    }

    private fun LineDto.toDomain(): Line {
        val mode = when (transportMode.uppercase()) {
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
            authority = this.transportAuthority.name
        )
    }
}