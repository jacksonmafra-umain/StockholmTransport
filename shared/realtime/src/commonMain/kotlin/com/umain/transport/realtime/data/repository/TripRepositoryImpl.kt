package com.umain.transport.realtime.data.repository

import com.umain.transport.core.data.DataResult
import com.umain.transport.core.data.NetworkError
import com.umain.transport.core.logging.AppLogger
import com.umain.transport.realtime.RealtimeConfig
import com.umain.transport.realtime.data.model.ActiveTripDto
import com.umain.transport.realtime.data.model.TripDto
import com.umain.transport.realtime.data.remote.TripUpdateDataSource
import com.umain.transport.realtime.domain.model.ActiveTrip
import com.umain.transport.realtime.domain.model.Station
import com.umain.transport.realtime.domain.model.Trip
import com.umain.transport.realtime.domain.model.TripDisplayInfo
import com.umain.transport.realtime.domain.repository.TripRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.io.IOException

internal class TripRepositoryImpl(
    private val httpClient: HttpClient,
    private val dataSource: TripUpdateDataSource,
    private val config: RealtimeConfig,
) : TripRepository {

    private val tag = "TripRepository"

    override fun getTripUpdates(tripId: String): Flow<DataResult<TripDisplayInfo>> =
        dataSource.connect(tripId)
            .map<_, DataResult<TripDisplayInfo>> { dto ->
                DataResult.Success(
                    TripDisplayInfo(
                        currentStation = Station(dto.currentStop.name),
                        nextStations = dto.nextThreeStops.map { Station(it.name) },
                        finalDestination = Station(dto.finalDestination.name),
                    ),
                )
            }
            .catch { e ->
                AppLogger.e(tag, "trip stream failed for $tripId", e)
                emit(DataResult.Error(e.toNetworkError()))
            }

    override suspend fun getActiveTrips(): DataResult<List<ActiveTrip>> = safeRequest {
        val dtos = httpClient
            .get("${config.httpBaseUrl}/api/trips/active")
            .body<List<ActiveTripDto>>()
        dtos.map {
            ActiveTrip(
                tripId = it.tripId,
                lineId = it.lineId,
                lineNumber = it.lineNumber,
                transportMode = it.transportMode,
            )
        }
    }

    override suspend fun startTrip(lineId: String): DataResult<Trip> = safeRequest {
        val dto = httpClient
            .post("${config.httpBaseUrl}/trip/start/$lineId")
            .body<TripDto>()
        Trip(
            id = dto.id,
            lineId = dto.lineId,
            startTime = dto.startTime,
            status = dto.status,
        )
    }

    /**
     * Wraps a one-shot HTTP call in DataResult, mapping Ktor exceptions
     * onto the same NetworkError sealed class the rest of the library
     * uses (lines/sites/departures/etc.).
     */
    private inline fun <T> safeRequest(block: () -> T): DataResult<T> = try {
        DataResult.Success(block())
    } catch (e: Exception) {
        AppLogger.e(tag, "request failed", e)
        DataResult.Error(e.toNetworkError())
    }
}

private fun Throwable.toNetworkError(): NetworkError = when (this) {
    is HttpRequestTimeoutException -> NetworkError.Timeout
    is IOException -> NetworkError.NoInternet
    is ResponseException -> when (response.status.value) {
        404 -> NetworkError.NotFound
        in 500..599 -> NetworkError.ServerError
        else -> NetworkError.Unknown("HTTP ${response.status.value}")
    }
    else -> NetworkError.Unknown(message ?: "Unknown error")
}
