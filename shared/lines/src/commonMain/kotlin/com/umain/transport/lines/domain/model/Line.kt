package com.umain.transport.lines.domain.model

data class Line(
    val id: Int,
    val name: String,
    val designation: String,
    val transportMode: TransportMode,
    val authority: String,
)

enum class TransportMode {
    METRO,
    TRAM,
    TRAIN,
    BUS,
    SHIP,
    FERRY,
    TAXI,
    UNKNOWN,
}
