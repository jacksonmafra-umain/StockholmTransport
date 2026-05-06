package com.umain.transport.realtime.domain.repository

import com.umain.transport.realtime.domain.model.ActiveTrip
import com.umain.transport.realtime.domain.model.Trip
import com.umain.transport.realtime.domain.model.TripDisplayInfo
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun getTripUpdates(tripId: String): Flow<TripDisplayInfo>

    suspend fun startTrip(lineId: String): Trip
    suspend fun getActiveTrips(): List<ActiveTrip>
}