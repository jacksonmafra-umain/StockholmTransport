package com.umain.transport.realtime.domain.repository

import com.umain.transport.core.data.DataResult
import com.umain.transport.realtime.domain.model.ActiveTrip
import com.umain.transport.realtime.domain.model.Trip
import com.umain.transport.realtime.domain.model.TripDisplayInfo
import kotlinx.coroutines.flow.Flow

/**
 * Source-of-truth contract for the realtime trip data plane. Implementations:
 *   - WebSocket-backed (current talk default — listens to /updates/{tripId})
 *   - Future on-device tick loop using a packaged [Timetable] (offline trips)
 *
 * Every method returns [DataResult] so consumers (Compose / React / Swift)
 * never have to wrap calls in try/catch. Stream method emits a sequence of
 * [DataResult]s — one per tick — so transient WS failures surface as
 * `Error` frames without tearing down the Flow.
 */
interface TripRepository {
    /**
     * Live-updating display payload for a single trip. Each emission is one
     * "where is this train" frame; subscribers re-render on every tick.
     */
    fun getTripUpdates(tripId: String): Flow<DataResult<TripDisplayInfo>>

    /** List of trips currently being simulated by the upstream server. */
    suspend fun getActiveTrips(): DataResult<List<ActiveTrip>>

    /** Start a new simulated trip on the given line. */
    suspend fun startTrip(lineId: String): DataResult<Trip>
}
