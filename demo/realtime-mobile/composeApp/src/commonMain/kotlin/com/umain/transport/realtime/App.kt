package com.umain.transport.realtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.umain.transport.realtime.di.initKoin
import com.umain.transport.realtime.theme.AppTheme
import com.umain.transport.realtime.ui.TripScreen
import com.umain.transport.realtime.ui.TripSelectionScreen

@Composable
fun App() {
    initKoin()

    // Force dark theme — the SL line palette reads as wayfinding signage on a
    // tunnel-dark background; light mode washes the metro reds and blues out.
    AppTheme(darkTheme = true) {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.TripSelection) }

        when (val screen = currentScreen) {
            Screen.TripSelection -> TripSelectionScreen(
                onTripSelected = { trip ->
                    currentScreen = Screen.TripDisplay(
                        tripId = trip.tripId,
                        lineNumber = trip.lineNumber,
                        transportMode = trip.transportMode,
                    )
                },
            )
            is Screen.TripDisplay -> TripScreen(
                tripId = screen.tripId,
                lineNumber = screen.lineNumber,
                transportMode = screen.transportMode,
                onBack = { currentScreen = Screen.TripSelection },
            )
        }
    }
}

private sealed interface Screen {
    data object TripSelection : Screen
    data class TripDisplay(
        val tripId: String,
        val lineNumber: String,
        val transportMode: String,
    ) : Screen
}
