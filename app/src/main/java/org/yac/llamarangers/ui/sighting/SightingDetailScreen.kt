package org.yac.llamarangers.ui.sighting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.yac.llamarangers.data.local.entity.InfestationZoneEntity
import org.yac.llamarangers.data.local.entity.TreatmentRecordEntity
import org.yac.llamarangers.domain.model.enums.InvasiveSpecies
import org.yac.llamarangers.domain.model.enums.TreatmentMethod
import org.yac.llamarangers.ui.components.SyncStatusBadge
import org.yac.llamarangers.ui.components.VariantColourDot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.graphics.Color

/**
 * Detailed view of a single sighting with treatments and zone assignment.
 * Ports iOS SightingDetailView — M3 polish pass.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SightingDetailScreen(
    viewModel: SightingDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToTreatmentEntry: (String) -> Unit = {}
) {
    val sighting by viewModel.sighting.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val treatments by viewModel.treatments.collectAsState()
    val allZones by viewModel.allZones.collectAsState()
    var showZonePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sighting Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    sighting?.let { s ->
                        TextButton(onClick = { onNavigateToTreatmentEntry(s.id) }) {
                            Text("Add Treatment")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val s = sighting
        if (s == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    Text("Loading…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Not found", color = MaterialTheme.colorScheme.error)
                }
            }
            return@Scaffold
        }

        val species = viewModel.species
        val size = viewModel.size
        val syncStatus = viewModel.syncStatus
        val assignedZone = viewModel.assignedZone

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Metadata card ─────────────────────────────────────────────────
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Variant name row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VariantColourDot(variant = species, size = 20.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = species.displayName,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        SyncStatusBadge(status = syncStatus)
                    }

                    // Date
                    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
                    Text(
                        text = dateFormat.format(Date(s.createdAt)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Coordinates
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.6f, %.6f", s.latitude, s.longitude),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = String.format("Accuracy \u00B1%.0fm", s.horizontalAccuracy),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Size chip
                    AssistChip(
                        onClick = {},
                        label = { Text("${size.displayName} \u2014 ${size.areaDescription}") }
                    )
                }
            }

            // ── Notes ─────────────────────────────────────────────────────────
            val notes = s.notes
            if (!notes.isNullOrBlank()) {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = notes, style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider()
            }

            // ── Photos ────────────────────────────────────────────────────────
            val photos = viewModel.photoFilenames
            if (photos.isNotEmpty()) {
                Text(
                    text = "Photos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    photos.forEach { filename ->
                        PhotoThumbnail(
                            filename = filename,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
                HorizontalDivider()
            }

            // ── Zone assignment ───────────────────────────────────────────────
            Text(
                text = "Zone",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = assignedZone?.name ?: "Unassigned",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (assignedZone != null)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    if (assignedZone == null) {
                        FilledTonalButton(onClick = {
                            viewModel.loadZones()
                            showZonePicker = true
                        }) {
                            Text("Assign to Zone")
                        }
                    } else {
                        TextButton(onClick = {
                            viewModel.loadZones()
                            showZonePicker = true
                        }) {
                            Text("Change")
                        }
                    }
                }
            }

            // ── Treatments ────────────────────────────────────────────────────
            Text(
                text = "Treatments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (treatments.isEmpty()) {
                Text(
                    text = "No treatments recorded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                treatments.forEach { treatment ->
                    TreatmentCard(treatment = treatment)
                    if (treatment.outcomeNotes?.startsWith("📷 After:") == true) {
                        BeforeAfterCard(
                            sighting = s,
                            treatmentNotes = treatment.outcomeNotes!!,
                            species = viewModel.species
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Zone picker bottom sheet
    if (showZonePicker) {
        ZonePickerSheet(
            zones = allZones,
            currentZoneId = sighting?.infestationZoneId,
            onSelect = { zoneId ->
                viewModel.assignToZone(zoneId)
                showZonePicker = false
            },
            onDismiss = { showZonePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZonePickerSheet(
    zones: List<InfestationZoneEntity>,
    currentZoneId: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Assign to Zone",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Unassigned option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(null) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Unassigned",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (currentZoneId == null) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            HorizontalDivider()

            zones.forEach { zone ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(zone.id) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VariantColourDot(
                        variant = InvasiveSpecies.fromValue(zone.dominantVariant ?: ""),
                        size = 12.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = zone.name ?: "Unnamed Zone",
                        modifier = Modifier.weight(1f)
                    )
                    if (zone.id == currentZoneId) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                HorizontalDivider()
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TreatmentCard(treatment: TreatmentRecordEntity) {
    val method = TreatmentMethod.fromValue(treatment.method)
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = method.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = dateFormat.format(Date(treatment.treatmentDate)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val notes = treatment.outcomeNotes
            if (!notes.isNullOrBlank()) {
                val display = if (notes.startsWith("📷 After:")) stripPhotoPrefix(notes) else notes
                if (display.isNotBlank()) {
                    Text(
                        text = display,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun stripPhotoPrefix(notes: String): String {
    val range = notes.indexOf(". ")
    if (range == -1) return ""
    return notes.substring(range + 2)
}

// ── Before/After Card ─────────────────────────────────────────────────────────

@Composable
private fun BeforeAfterCard(
    sighting: org.yac.llamarangers.data.local.entity.SightingLogEntity,
    treatmentNotes: String,
    species: InvasiveSpecies
) {
    val afterCount = extractAfterCount(treatmentNotes)
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HStack(spacing = 8.dp) {
                Icon(
                    Icons.Default.Check, // Closest to arrow.left.arrow.right without external library
                    contentDescription = null,
                    tint = Color(0xFFC4692A), // dsAccent
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "Before / After",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Comparison",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFC4692A),
                    modifier = Modifier
                        .background(Color(0xFFC4692A).copy(alpha = 0.12f), shape = androidx.compose.foundation.shape.CircleShape)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Before column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(species.color.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check, // Need actual icon matching species.iconName ideally, fallback to check
                            contentDescription = null,
                            tint = species.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text("Before", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text("Sighting logged", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dateFormat.format(Date(sighting.createdAt)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(100.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // After column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF2A7A4A).copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.CircleShape), // dsStatusCleared
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF2A7A4A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text("After", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text("Treatment applied", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$afterCount photo(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun extractAfterCount(notes: String): Int {
    val start = notes.indexOf("📷 After: ")
    if (start == -1) return 0
    val end = notes.indexOf(" photo", start)
    if (end == -1) return 0
    return notes.substring(start + 10, end).toIntOrNull() ?: 0
}

// Helpers for VStack/HStack matching SwiftUI layout behavior
@Composable
private fun HStack(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    spacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable RowScope.() -> Unit
) {
    val arrangement = if (spacing > 0.dp) Arrangement.spacedBy(spacing) else horizontalArrangement
    Row(
        modifier = modifier,
        horizontalArrangement = arrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}


