package com.umain.transport.realtime

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.umain.transport.realtime.di.initKoin
import com.umain.transport.realtime.ui.TripScreen
import com.umain.transport.realtime.ui.TripSelectionScreen

@Composable
fun App() {
    initKoin()

    MaterialTheme {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.TripSelection) }

        when (val screen = currentScreen) {
            is Screen.TripSelection -> {
                TripSelectionScreen(
                    onTripSelected = { tripId ->
                        currentScreen = Screen.TripDisplay(tripId)
                    }
                )
            }
            is Screen.TripDisplay -> {
                TripScreen(tripId = screen.tripId)
            }
        }
    }
}

// Define os estados de navegação
sealed class Screen {
    object TripSelection : Screen()
    data class TripDisplay(val tripId: String) : Screen()
}