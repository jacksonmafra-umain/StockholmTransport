package com.umain.transport.realtime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.umain.transport.realtime.domain.model.Station
import com.umain.transport.realtime.presentation.TripViewModel
import com.umain.transport.realtime.theme.SLLines
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripScreen(
    tripId: String,
    lineNumber: String,
    transportMode: String,
    onBack: () -> Unit,
    viewModel: TripViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val lineColor = SLLines.colorFor(transportMode, lineNumber)
    val groupName = SLLines.lineGroupNameFor(transportMode, lineNumber)
        ?: transportMode.replaceFirstChar { it.titlecase() }

    LaunchedEffect(key1 = tripId) {
        viewModel.startObservingTrip(tripId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinePuck(lineNumber = lineNumber, lineColor = lineColor)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(groupName, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "Live trip",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(color = lineColor)
                uiState.error != null -> Text(
                    uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
                uiState.displayInfo != null -> {
                    val info = uiState.displayInfo!!
                    StationRail(
                        currentStation = info.currentStation,
                        nextStations = info.nextStations,
                        finalDestination = info.finalDestination,
                        lineColor = lineColor,
                    )
                }
            }
        }
    }
}

/** Circular line designation badge in SL signage style. */
@Composable
private fun LinePuck(lineNumber: String, lineColor: Color) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(lineColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = lineNumber,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

/**
 * Vertical metro-line metaphor: a coloured track on the left with station
 * dots; current station big and bright, next three smaller, terminus dot
 * filled in solid. Mirrors how stations are drawn on real SL maps.
 *
 * Stations are the library's [Station] domain type; we read `.name` once at
 * the leaf (StationRow) so the UI stays decoupled from any future fields.
 */
@Composable
private fun StationRail(
    currentStation: Station,
    nextStations: List<Station>,
    finalDestination: Station,
    lineColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingValues(horizontal = 24.dp, vertical = 24.dp)),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        StationRow(
            name = currentStation.name,
            sublabel = "You are here",
            lineColor = lineColor,
            isCurrent = true,
            connectorBelow = nextStations.isNotEmpty() || finalDestination.name.isNotEmpty(),
        )

        nextStations.take(3).forEachIndexed { idx, station ->
            StationRow(
                name = station.name,
                sublabel = "Next ${idx + 1}",
                lineColor = lineColor,
                isCurrent = false,
                connectorBelow = idx < nextStations.size - 1 ||
                    (finalDestination.name.isNotEmpty() && finalDestination.name != station.name),
            )
        }

        if (finalDestination.name.isNotEmpty() && finalDestination.name != currentStation.name) {
            StationRow(
                name = finalDestination.name,
                sublabel = "Final destination",
                lineColor = lineColor,
                isCurrent = false,
                isTerminal = true,
                connectorBelow = false,
            )
        }
    }
}

@Composable
private fun StationRow(
    name: String,
    sublabel: String,
    lineColor: Color,
    isCurrent: Boolean = false,
    isTerminal: Boolean = false,
    connectorBelow: Boolean = true,
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Track + dot column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp),
        ) {
            val dotSize = if (isCurrent) 22.dp else if (isTerminal) 18.dp else 14.dp
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(if (isCurrent || isTerminal) lineColor else MaterialTheme.colorScheme.surface),
            ) {
                if (!isCurrent && !isTerminal) {
                    // Hollow dot for next-stops — line-coloured ring on dark surface.
                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(dotSize - 4.dp)
                                .clip(CircleShape)
                                .background(lineColor),
                        )
                    }
                }
            }
            if (connectorBelow) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp, bottom = 2.dp)
                        .width(4.dp)
                        .height(if (isCurrent) 56.dp else 40.dp)
                        .background(lineColor),
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        // Station name + label
        Column(
            modifier = Modifier
                .padding(top = if (isCurrent) 0.dp else 2.dp, bottom = 8.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = sublabel.uppercase(),
                color = lineColor,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = name,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = if (isCurrent) 36.sp else 22.sp,
                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                maxLines = 2,
            )
        }
    }
}
