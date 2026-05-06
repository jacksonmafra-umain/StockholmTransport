package com.umain.transport.realtime.theme

import androidx.compose.ui.graphics.Color

/**
 * Stockholm public transport wayfinding palette.
 *
 * Source: SL's official line-coloring scheme as catalogued by
 * https://studyinsweden.se/blogs/2022/10/31/stockholm-metro/ and the SL design
 * guidelines themselves. The metro is split into three named lines (Blue, Red,
 * Green) by line designation, not transport mode — the realtime API gives us
 * `lineNumber` (e.g. "10", "13", "17") and `transportMode` ("metro", "train",
 * "tram", "bus", "ship", "ferry") and we colour by both.
 */
object SLLines {
    // Tunnelbanan — Stockholm metro
    val MetroBlue = Color(0xFF0064A8)   // Blå linjen — T10, T11
    val MetroRed = Color(0xFFD71920)    // Röda linjen — T13, T14
    val MetroGreen = Color(0xFF00853E)  // Gröna linjen — T17, T18, T19

    // Pendeltåg — commuter rail (40, 41, 42, 43, 44, 45, 48)
    val Train = Color(0xFFB25196)       // SL pink-violet

    // Tvärbanan / Spårväg — light rail tram (7, 12, 21, 22, 25, 26, 27, 30, 31)
    val Tram = Color(0xFFE97713)        // SL orange

    // Bus — Stadsbuss (red), Blåbuss (blue). Default bus = red, override if
    // you want to surface Blåbuss differently in a future iteration.
    val Bus = Color(0xFFD71920)

    // Sjötrafik — ferries / ships
    val Ship = Color(0xFF008CB4)        // SL waterway cyan
    val Ferry = Color(0xFF008CB4)

    // Fallback when mode/lineNumber don't match anything known.
    val Fallback = Color(0xFF787878)

    /**
     * Pick the wayfinding colour for a (mode, lineNumber) pair.
     *
     * Metro is split: 10/11 → blue, 13/14 → red, 17/18/19 → green. Everything
     * else dispatches on transport mode.
     */
    fun colorFor(mode: String, lineNumber: String): Color {
        val normalizedMode = mode.lowercase()
        val normalizedLine = lineNumber.trim().removePrefix("T")
        return when (normalizedMode) {
            "metro" -> when (normalizedLine) {
                "10", "11" -> MetroBlue
                "13", "14" -> MetroRed
                "17", "18", "19" -> MetroGreen
                else -> MetroBlue
            }
            "train" -> Train
            "tram" -> Tram
            "bus" -> Bus
            "ship" -> Ship
            "ferry" -> Ferry
            else -> Fallback
        }
    }

    /**
     * Human-readable line-group name (e.g. "Blå linjen") for display in
     * headers / chips. Returns null when there's no canonical name (commuter
     * trains, trams, buses use the line number directly).
     */
    fun lineGroupNameFor(mode: String, lineNumber: String): String? {
        if (mode.lowercase() != "metro") return null
        return when (lineNumber.trim().removePrefix("T")) {
            "10", "11" -> "Blå linjen"
            "13", "14" -> "Röda linjen"
            "17", "18", "19" -> "Gröna linjen"
            else -> null
        }
    }
}
