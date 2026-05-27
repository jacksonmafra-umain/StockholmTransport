package com.umain.transport.realtime.domain.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * A bus, train, tram, or boat actively running on a line — either being
 * advanced by the server's SimulationEngine or by a future on-device tick
 * loop (the offline-trips angle the talk hints at).
 *
 * Position is a [longitude, latitude] pair as floats so JS consumers can
 * pipe it straight into Mapbox/Leaflet without intermediate parsing.
 *
 * The fields mirror the realtime simulator's Mongoose Vehicle schema, but
 * stripped to the JS-friendly subset — no Mongo ObjectIds, no nested
 * GeoJSON wrappers, no cross-collection refs.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
data class Vehicle(
    val id: String,
    val lineId: String,
    val mode: String,
    val status: String,
    val currentStopIndex: Int,
    val progressBetweenStops: Double,
    val direction: Int,
    val longitude: Double,
    val latitude: Double,
    /**
     * Epoch ms as Double — Kotlin/JS in 2.3 can't @JsExport `Long` without
     * BigInt support, and BigInt breaks `JSON.stringify` for the same
     * reason `Line.transportMode` is a String instead of a Kotlin enum.
     * Double has 53 bits of integer precision, so timestamps stay accurate
     * past the year 287,000.
     */
    val lastUpdateMillis: Double,
)
