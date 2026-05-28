package com.umain.transport.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun ItemListScreen(
    moduleId: String,
    onBackPressed: () -> Unit,
    onItemSelected: (moduleId: String, itemId: String) -> Unit,
) {
    val title = allModules.find { it.id == moduleId }?.title ?: "Items"

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
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when (moduleId) {
                ModuleItem.Lines.id -> LinesList(onItemSelected = { itemId -> onItemSelected(moduleId, itemId) })
                ModuleItem.Sites.id -> SitesList(onItemSelected = { itemId -> onItemSelected(moduleId, itemId) })
                ModuleItem.Departures.id -> DeparturesList(onItemSelected = { itemId -> onItemSelected(moduleId, itemId) })
                ModuleItem.StopPoints.id -> StopPointsList(onItemSelected = { itemId -> onItemSelected(moduleId, itemId) })
                ModuleItem.Authorities.id -> AuthoritiesList(onItemSelected = { itemId -> onItemSelected(moduleId, itemId) })
                else -> Text("Module not found")
            }
        }
    }
}

@Composable
private fun LinesList(
    viewModel: LinesViewModel = koinInject(),
    onItemSelected: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadLines() }

    when {
        state.isLoading -> CircularProgressIndicator()
        state.error != null -> Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
        else -> {
            // LinesUiState exposes a flat List<Line> (transportMode is a String, not an enum)
            // so the shape survives the Kotlin → JS boundary. Consumers regroup as needed.
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                state.lines.groupBy { it.transportMode }.forEach { (mode, lines) ->
                    item {
                        Text(mode, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(lines, key = { it.id }) { line ->
                        ListItem(title = "Line ${line.designation}", subtitle = line.name, onClick = { onItemSelected(line.id.toString()) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SitesList(
    viewModel: SitesViewModel = koinInject(),
    onItemSelected: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadSites() }

    when {
        state.isLoading -> CircularProgressIndicator()
        state.error != null -> Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
        else -> {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(state.sites, key = { it.id }) { site ->
                    ListItem(title = site.name, subtitle = "ID: ${site.id}", onClick = { onItemSelected(site.id.toString()) })
                }
            }
        }
    }
}

@Composable
private fun DeparturesList(
    viewModel: DeparturesViewModel = koinInject(),
    onItemSelected: (String) -> Unit,
) {
    val demoSiteId = 9192 // Slussen
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadDepartures(demoSiteId) }

    when {
        state.isLoading -> CircularProgressIndicator()
        state.error != null -> Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
        else -> {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(state.departures) { departure ->
                    val uniqueId = "${departure.lineDesignation}-${departure.destination}-${departure.displayTime}"
                    ListItem(
                        title = "${departure.lineDesignation} to ${departure.destination}",
                        subtitle = "Leaves at: ${departure.displayTime}",
                        onClick = { onItemSelected(uniqueId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun StopPointsList(
    viewModel: StopPointsViewModel = koinInject(),
    onItemSelected: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadStopPoints() }

    when {
        state.isLoading -> CircularProgressIndicator()
        state.error != null -> Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
        else -> {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(state.stopPoints, key = { it.id }) { stopPoint ->
                    ListItem(
                        title = stopPoint.name,
                        subtitle = "Area: ${stopPoint.stopAreaName}",
                        onClick = { onItemSelected(stopPoint.id.toString()) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthoritiesList(
    viewModel: AuthoritiesViewModel = koinInject(),
    onItemSelected: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadAuthorities() }

    when {
        state.isLoading -> CircularProgressIndicator()
        state.error != null -> Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
        else -> {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(state.authorities, key = { it.id }) { authority ->
                    ListItem(
                        title = authority.name,
                        subtitle = authority.formalName ?: "N/A",
                        onClick = { onItemSelected(authority.id.toString()) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ListItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
