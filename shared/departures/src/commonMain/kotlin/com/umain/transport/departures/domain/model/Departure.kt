package com.umain.transport.departures.domain.model

data class Departure(
    val lineDesignation: String,
    val destination: String,
    val displayTime: String,
    val transportMode: String,
)
