package com.umain.transport.stoppoints.data.repository

import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.core.logging.AppLogger
import com.umain.transport.core.network.API_BASE_URL
import com.umain.transport.stoppoints.data.model.StopPointDto
import com.umain.transport.stoppoints.domain.model.StopPoint
import com.umain.transport.stoppoints.domain.repository.StopPointsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.utils.io.errors.IOException

class StopPointsRepositoryImpl(private val httpClient: HttpClient) : StopPointsRepository {
    private val tag = "StopPointsRepository"

    override suspend fun getAllStopPoints(): DataResult<List<StopPoint>> {
        return try {
            val response = httpClient.get("$API_BASE_URL/stop-points").body<List<StopPointDto>>()
            DataResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            AppLogger.e(tag, "Failed to fetch all stop points", e)
            val networkError = when (e) {
                is HttpRequestTimeoutException -> NetworkError.Timeout
                is IOException -> NetworkError.NoInternet
                else -> NetworkError.Unknown(e.message ?: "An unknown error occurred")
            }
            DataResult.Error(networkError)
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