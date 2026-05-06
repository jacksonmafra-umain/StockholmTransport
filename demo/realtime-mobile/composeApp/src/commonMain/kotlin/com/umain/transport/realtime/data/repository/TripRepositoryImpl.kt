package com.umain.transport.realtime.data.repository

import com.umain.transport.realtime.config.BuildConfig
import com.umain.transport.realtime.data.remote.TripUpdateDataSource
import com.umain.transport.realtime.domain.model.ActiveTrip
import com.umain.transport.realtime.domain.model.Trip
import com.umain.transport.realtime.domain.model.TripDisplayInfo
import com.umain.transport.realtime.domain.repository.TripRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripRepositoryImpl(
    private val dataSource: TripUpdateDataSource,
    private val httpClient: HttpClient
) : TripRepository {

    override fun getTripUpdates(tripId: String): Flow<TripDisplayInfo> =
        dataSource.connect(tripId).map { message ->
            TripDisplayInfo(
                currentStation = message.currentStation.name,
                lineNumber = "",
                nextStations = message.nextThreeStops.map { it.name },
                finalDestination = message.finalDestination.name,
            )
        }

    override suspend fun startTrip(lineId: String): Trip {
        val url = "${BuildConfig.SERVER_HOST_URL}/trip/start/$lineId"
        return httpClient.get(url).body()
    }

    override suspend fun getActiveTrips(): List<ActiveTrip> =
        httpClient.get("${BuildConfig.SERVER_HOST_URL}/api/trips/active").body()
}