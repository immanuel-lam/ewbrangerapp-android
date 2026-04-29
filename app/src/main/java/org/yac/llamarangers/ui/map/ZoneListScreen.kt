package org.yac.llamarangers.ui.map

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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.hilt.navigation.compose.hiltViewModel
import org.yac.llamarangers.data.local.entity.InfestationZoneEntity
import org.yac.llamarangers.domain.model.enums.InvasiveSpecies
import org.yac.llamarangers.ui.components.VariantColourDot
import org.yac.llamarangers.ui.theme.RangerGreen
import org.yac.llamarangers.ui.theme.RangerOrange
import org.yac.llamarangers.ui.theme.RangerRed

/**
 * List of infestation zones.
 * Ports iOS ZoneListView.
 * Each zone shown as an ElevatedCard with name, status chip, and sighting count.
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
    val sightingCounts by viewModel.sightingCountsByZone.collectAsState()

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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            items(zones, key = { it.id }) { zone ->
                ZoneCard(
                    zone = zone,
                    sightingCount = sightingCounts[zone.id] ?: 0,
                    onClick = { onNavigateToZoneDetail(zone.id) }
                )
            }
        }
    }
}

@Composable
private fun ZoneCard(
    zone: InfestationZoneEntity,
    sightingCount: Int,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VariantColourDot(
                variant = InvasiveSpecies.fromValue(zone.dominantVariant ?: ""),
                size = 14.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = zone.name ?: "Unnamed Zone",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$sightingCount ${if (sightingCount == 1) "sighting" else "sightings"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            ZoneStatusChip(status = zone.status)
        }
    }
}

@Composable
private fun ZoneStatusChip(status: String?) {
    val (label, containerColor, labelColor) = when (status) {
        "underTreatment" -> Triple("Treating", RangerOrange.copy(alpha = 0.15f), RangerOrange)
        "cleared" -> Triple("Cleared", RangerGreen.copy(alpha = 0.15f), RangerGreen)
        else -> Triple("Active", RangerRed.copy(alpha = 0.15f), RangerRed)
    }
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = labelColor.copy(alpha = 0.4f)
        )
    )
}

private fun statusLabel(status: String?): String = when (status) {
    "underTreatment" -> "Under Treatment"
    "cleared" -> "Cleared"
    else -> "Active"
}
