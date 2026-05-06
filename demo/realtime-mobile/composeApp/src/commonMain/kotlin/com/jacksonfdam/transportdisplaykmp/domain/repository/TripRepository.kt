package com.jacksonfdam.transportdisplaykmp.domain.repository

import com.jacksonfdam.transportdisplaykmp.domain.model.ActiveTrip
import com.jacksonfdam.transportdisplaykmp.domain.model.Trip
import com.jacksonfdam.transportdisplaykmp.domain.model.TripDisplayInfo
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun getTripUpdates(tripId: String): Flow<TripDisplayInfo>

    suspend fun startTrip(lineId: String): Trip
    suspend fun getActiveTrips(): List<ActiveTrip>
}