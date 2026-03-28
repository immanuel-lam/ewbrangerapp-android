package org.yac.llamarangers.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.local.entity.InfestationZoneEntity
import org.yac.llamarangers.data.local.entity.InfestationZoneSnapshotEntity
import org.yac.llamarangers.data.local.entity.SightingLogEntity
import org.yac.llamarangers.data.repository.SightingRepository
import org.yac.llamarangers.data.repository.ZoneRepository
import org.yac.llamarangers.domain.model.enums.LantanaVariant
import org.yac.llamarangers.ui.components.VariantColourDot
import org.yac.llamarangers.ui.theme.RangerGreen
import org.yac.llamarangers.ui.theme.RangerOrange
import org.yac.llamarangers.ui.theme.RangerRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Zone detail showing info, boundary snapshots, and linked sightings.
 * Ports iOS ZoneDetailView.
 * Info and snapshot sections use Card; snapshots are OutlinedCard items.
 */

@HiltViewModel
class ZoneDetailViewModel @Inject constructor(
    private val zoneRepository: ZoneRepository,
    private val sightingRepository: SightingRepository
) : ViewModel() {

    private val _zone = MutableStateFlow<InfestationZoneEntity?>(null)
    val zone: StateFlow<InfestationZoneEntity?> = _zone.asStateFlow()

    private val _snapshots = MutableStateFlow<List<InfestationZoneSnapshotEntity>>(emptyList())
    val snapshots: StateFlow<List<InfestationZoneSnapshotEntity>> = _snapshots.asStateFlow()

    private val _linkedSightings = MutableStateFlow<List<SightingLogEntity>>(emptyList())
    val linkedSightings: StateFlow<List<SightingLogEntity>> = _linkedSightings.asStateFlow()

    fun loadZone(zoneId: String) {
        viewModelScope.launch {
            val allZones = zoneRepository.fetchAllZones()
            _zone.value = allZones.firstOrNull { it.id == zoneId }
            _snapshots.value = zoneRepository.fetchSnapshotsForZone(zoneId)
                .sortedByDescending { it.snapshotDate }
            val allSightings = sightingRepository.fetchAllSightings()
            _linkedSightings.value = allSightings
                .filter { it.infestationZoneId == zoneId }
                .sortedByDescending { it.createdAt }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneDetailScreen(
    zoneId: String,
    viewModel: ZoneDetailViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val zone by viewModel.zone.collectAsState()
    val snapshots by viewModel.snapshots.collectAsState()
    val linkedSightings by viewModel.linkedSightings.collectAsState()
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val dateTimeFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())

    LaunchedEffect(zoneId) { viewModel.loadZone(zoneId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(zone?.name ?: "Zone Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            // Zone Info card
            item {
                Text(
                    text = "Zone Info",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    zone?.let { z ->
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VariantColourDot(
                                    variant = LantanaVariant.fromValue(z.dominantVariant ?: ""),
                                    size = 14.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    z.name ?: "Unnamed Zone",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                ZoneStatusBadge(z.status)
                            }
                            z.dominantVariant?.let { dv ->
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(8.dp))
                                Row {
                                    Text("Variant: ", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        LantanaVariant.fromValue(dv).displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                Text("Created: ", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    dateFormat.format(Date(z.createdAt)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Boundary Snapshots section
            item {
                Text(
                    text = "Boundary Snapshots (${snapshots.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }
            if (snapshots.isEmpty()) {
                item {
                    Text(
                        text = "No boundary drawn yet. Use Draw Zone Boundary on the map.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(snapshots, key = { it.id }) { snapshot ->
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                            Text(
                                text = dateTimeFormat.format(Date(snapshot.snapshotDate)),
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "%.0f m\u00B2".format(snapshot.area),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Linked Sightings section
            item {
                Text(
                    text = "Linked Sightings (${linkedSightings.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }
            if (linkedSightings.isEmpty()) {
                item {
                    Text(
                        text = "No sightings assigned to this zone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(linkedSightings, key = { it.id }) { sighting ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VariantColourDot(
                            variant = LantanaVariant.fromValue(sighting.variant),
                            size = 10.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LantanaVariant.fromValue(sighting.variant).displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = dateFormat.format(Date(sighting.createdAt)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = sighting.infestationSize,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ZoneStatusBadge(status: String?) {
    val (label, containerColor, labelColor) = when (status) {
        "underTreatment" -> Triple("Treating", RangerOrange.copy(alpha = 0.15f), RangerOrange)
        "cleared" -> Triple("Cleared", RangerGreen.copy(alpha = 0.15f), RangerGreen)
        else -> Triple("Active", RangerRed.copy(alpha = 0.15f), RangerRed)
    }
    androidx.compose.material3.AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor
            )
        },
        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
            containerColor = containerColor
        ),
        border = androidx.compose.material3.AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = labelColor.copy(alpha = 0.4f)
        )
    )
}
