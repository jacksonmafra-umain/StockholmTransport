package com.umain.transport.stoppoints.data.repository

import com.umain.transport.core.network.API_BASE_URL
import com.umain.transport.stoppoints.data.model.StopPointDto
import com.umain.transport.stoppoints.domain.model.StopPoint
import com.umain.transport.stoppoints.domain.repository.StopPointsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class StopPointsRepositoryImpl(private val httpClient: HttpClient) : StopPointsRepository {
    override suspend fun getAllStopPoints(): Result<List<StopPoint>> {
        return try {
            val response = httpClient.get("$API_BASE_URL/stop-points").body<List<StopPointDto>>()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun StopPointDto.toDomain(): StopPoint {
        return StopPoint(
            id = this.stopPointNumber,
            name = this.stopPointName,
            zone = this.zoneShortName
        )
    }
}