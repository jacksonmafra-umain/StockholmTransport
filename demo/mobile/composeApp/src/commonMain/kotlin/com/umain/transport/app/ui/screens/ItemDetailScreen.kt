package com.umain.transport.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.umain.transport.app.model.ModuleItem
import com.umain.transport.app.model.allModules
import com.umain.transport.authorities.presentation.AuthoritiesViewModel
import com.umain.transport.departures.presentation.DeparturesViewModel
import com.umain.transport.lines.presentation.LinesViewModel
import com.umain.transport.sites.presentation.SitesViewModel
import com.umain.transport.stoppoints.presentation.StopPointsViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    moduleId: String,
    itemId: String,
    onBackPressed: () -> Unit,
) {
    val title = allModules.find { it.id == moduleId }?.title?.replaceFirst("s", "") ?: "Detail"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
        ) {
            when (moduleId) {
                ModuleItem.Lines.id -> LineDetail(itemId)
                ModuleItem.Sites.id -> SiteDetail(itemId)
                ModuleItem.Departures.id -> DepartureDetail(itemId)
                ModuleItem.StopPoints.id -> StopPointDetail(itemId)
                ModuleItem.Authorities.id -> AuthorityDetail(itemId)
                else -> Text("Item not found")
            }
        }
    }
}

@Composable
private fun LineDetail(
    itemId: String,
    viewModel: LinesViewModel = koinInject(),
) {
    val state by viewModel.uiState.collectAsState()
    // LinesUiState is a flat List<Line> with transportMode as String (see LinesViewModel).
    val line = state.lines.find { it.id.toString() == itemId }

    if (line != null) {
        DetailText("Designation:", line.designation)
        DetailText("Name:", line.name)
        DetailText("Transport Mode:", line.transportMode)
        DetailText("Authority:", line.authority)
    } else {
        Text("Line with ID $itemId not found.")
    }
}

@Composable
private fun SiteDetail(
    itemId: String,
    viewModel: SitesViewModel = koinInject(),
) {
    val state by viewModel.uiState.collectAsState()
    val site = state.sites.find { it.id.toString() == itemId }

    if (site != null) {
        DetailText("Name:", site.name)
        DetailText("ID:", site.id.toString())
        DetailText("Latitude:", site.latitude.toString())
        DetailText("Longitude:", site.longitude.toString())
    } else {
        Text("Site with ID $itemId not found.")
    }
}

@Composable
private fun DepartureDetail(
    itemId: String,
    viewModel: DeparturesViewModel = koinInject(),
) {
    val state by viewModel.uiState.collectAsState()
    val departure = state.departures.find { "${it.lineDesignation}-${it.destination}-${it.displayTime}" == itemId }

    if (departure != null) {
        DetailText("Line:", departure.lineDesignation)
        DetailText("Destination:", departure.destination)
        DetailText("Display Time:", departure.displayTime)
        DetailText("Transport Mode:", departure.transportMode)
    } else {
        Text("Departure with ID $itemId not found.")
    }
}

@Composable
private fun StopPointDetail(
    itemId: String,
    viewModel: StopPointsViewModel = koinInject(),
) {
    val state by viewModel.uiState.collectAsState()
    val stopPoint = state.stopPoints.find { it.id.toString() == itemId }

    if (stopPoint != null) {
        DetailText("Name:", stopPoint.name)
        DetailText("ID:", stopPoint.id.toString())
        DetailText("Type:", stopPoint.type)
        DetailText("Stop Area:", stopPoint.stopAreaName)
        DetailText("Authority:", stopPoint.authorityName)
        DetailText("Coordinates:", "(${stopPoint.latitude}, ${stopPoint.longitude})")
    } else {
        Text("Stop Point with ID $itemId not found.")
    }
}

@Composable
private fun AuthorityDetail(
    itemId: String,
    viewModel: AuthoritiesViewModel = koinInject(),
) {
    val state by viewModel.uiState.collectAsState()
    val authority = state.authorities.find { it.id.toString() == itemId }

    if (authority != null) {
        DetailText("Name:", authority.name)
        DetailText("ID:", authority.id.toString())
        DetailText("Formal Name:", authority.formalName ?: "N/A")
        DetailText("City:", authority.city ?: "N/A")
        DetailText("Country:", authority.country ?: "N/A")
    } else {
        Text("Authority with ID $itemId not found.")
    }
}

@Composable
private fun DetailText(
    label: String,
    value: String,
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}
