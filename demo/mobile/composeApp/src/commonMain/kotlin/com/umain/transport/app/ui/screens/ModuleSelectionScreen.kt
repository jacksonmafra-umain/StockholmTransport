package com.umain.transport.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umain.transport.app.model.ModuleItem
import com.umain.transport.app.model.allModules

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleSelectionScreen(onModuleSelected: (ModuleItem) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Select a Module") })
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding =
                androidx.compose.foundation.layout
                    .PaddingValues(16.dp),
        ) {
            items(allModules) { module ->
                ModuleCard(
                    module = module,
                    onClick = { onModuleSelected(module) },
                )
            }
        }
    }
}

@Composable
private fun ModuleCard(
    module: ModuleItem,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = module.title,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}
