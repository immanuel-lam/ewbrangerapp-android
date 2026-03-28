package org.yac.llamarangers.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.hilt.navigation.compose.hiltViewModel
import org.yac.llamarangers.data.local.entity.InfestationZoneEntity
import org.yac.llamarangers.domain.model.enums.LantanaVariant
import org.yac.llamarangers.ui.components.VariantColourDot

/**
 * List of infestation zones.
 * Ports iOS ZoneListView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneListScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToZoneDetail: (String) -> Unit = {},
    onNavigateToAddZone: () -> Unit = {}
) {
    val zones by viewModel.zones.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zones") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAddZone) {
                        Icon(Icons.Default.Add, contentDescription = "Add Zone")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(zones, key = { it.id }) { zone ->
                ZoneRow(
                    zone = zone,
                    onClick = { onNavigateToZoneDetail(zone.id) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun ZoneRow(
    zone: InfestationZoneEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VariantColourDot(
            variant = LantanaVariant.fromValue(zone.dominantVariant ?: ""),
            size = 14.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = zone.name ?: "Unnamed Zone",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = statusLabel(zone.status),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun statusLabel(status: String?): String = when (status) {
    "underTreatment" -> "Under Treatment"
    "cleared" -> "Cleared"
    else -> "Active"
}
