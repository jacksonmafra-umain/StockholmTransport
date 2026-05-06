package com.umain.transport.realtime.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for the realtime simulator's wire format. Matches the JSON shape
 * emitted by demo/realtime-api/application/SimulationEngine.js — current
 * stop + next-three + final-destination per WebSocket frame, plus the
 * /api/trips/active list shape on the HTTP side.
 *
 * Kept separate from the domain models because (a) the library should
 * never expose @Serializable types to consumers — they're an
 * implementation detail of how we talk to the wire — and (b) the wire
 * fields can drift independently of the domain shape.
 */

@Serializable
internal data class StationDto(
    val name: String = "",
)

/**
 * One frame off the WebSocket. The simulator emits this on every
 * SimulationEngine tick (default 5 s).
 */
@Serializable
internal data class WebSocketMessageDto(
    val type: String = "update",
    val tripId: String = "",
    @SerialName("currentStop") val currentStop: StationDto = StationDto(),
    val nextThreeStops: List<StationDto> = emptyList(),
    val finalDestination: StationDto = StationDto(),
)

/** Body of GET /api/trips/active */
@Serializable
internal data class ActiveTripDto(
    val tripId: String = "",
    val lineId: String = "",
    val lineNumber: String = "",
    val transportMode: String = "",
)

/** Body of POST /trip/start/{lineId} (Mongoose document shape). */
@Serializable
internal data class TripDto(
    @SerialName("_id") val id: String = "",
    @SerialName("line") val lineId: String = "",
    val startTime: String = "",
    val status: String = "ACTIVE",
)
