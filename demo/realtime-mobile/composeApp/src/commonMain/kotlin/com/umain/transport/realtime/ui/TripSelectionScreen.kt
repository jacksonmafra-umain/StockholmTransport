package com.umain.transport.realtime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umain.transport.realtime.domain.model.ActiveTrip
import com.umain.transport.realtime.presentation.TripSelectionViewModel
import com.umain.transport.realtime.theme.SLLines
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripSelectionScreen(
    onTripSelected: (ActiveTrip) -> Unit,
    viewModel: TripSelectionViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadActiveTrips()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active trips") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.error != null -> Text(
                    "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                )
                uiState.activeTrips.isEmpty() -> Text(
                    "No active trips. Bring up the simulator with `docker compose up`.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp),
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.activeTrips) { trip ->
                        TripItem(trip = trip, onClick = { onTripSelected(trip) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TripItem(trip: ActiveTrip, onClick: () -> Unit) {
    val lineColor = SLLines.colorFor(trip.transportMode, trip.lineNumber)
    val groupName = SLLines.lineGroupNameFor(trip.transportMode, trip.lineNumber)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Coloured rail on the leading edge — the Stockholm metro look.
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(96.dp)
                    .background(lineColor),
            )

            // Line "puck" — circular badge with the line number, like SL signage.
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(lineColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = trip.lineNumber,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                )
            }

            Column(
                modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = groupName ?: trip.transportMode.replaceFirstChar { it.titlecase() },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Trip #${trip.tripId.takeLast(6)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
