package org.yac.llamarangers.ui.more

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.yac.llamarangers.ui.theme.RangerGreen
import org.yac.llamarangers.ui.theme.RangerOrange
import org.yac.llamarangers.ui.theme.RangerRed
import org.yac.llamarangers.ui.theme.ZoneActive
import org.yac.llamarangers.ui.theme.ZoneCleared
import org.yac.llamarangers.ui.theme.ZoneUnderTreatment

/**
 * Dashboard screen showing aggregated statistics.
 * Ports iOS DashboardView — M3 polish pass.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val totalSightings by viewModel.totalSightings.collectAsStateWithLifecycle()
    val sightingsThisMonth by viewModel.sightingsThisMonth.collectAsStateWithLifecycle()
    val treatmentsThisMonth by viewModel.treatmentsThisMonth.collectAsStateWithLifecycle()
    val zoneStatusCounts by viewModel.zoneStatusCounts.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val lastSyncDate by viewModel.lastSyncDate.collectAsStateWithLifecycle()
    val rangerSightingCounts by viewModel.rangerSightingCounts.collectAsStateWithLifecycle()
    val clearedZonePercent by viewModel.clearedZonePercent.collectAsStateWithLifecycle()
    val openFollowUpTasks by viewModel.openFollowUpTasks.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Pending sync banner ───────────────────────────────────────────
            if (pendingSyncCount > 0) {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = RangerOrange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$pendingSyncCount records pending sync",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ── Stats grid ────────────────────────────────────────────────────
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            val statCards = listOf(
                Triple("Total\nSightings", "$totalSightings", RangerRed),
                Triple("This\nMonth", "$sightingsThisMonth", RangerOrange),
                Triple("Treatments\nThis Month", "$treatmentsThisMonth", MaterialTheme.colorScheme.primary),
                Triple("Zones\nCleared", "${clearedZonePercent.toInt()}%", RangerGreen),
                Triple("Open\nFollow-ups", "$openFollowUpTasks",
                    if (openFollowUpTasks > 0) RangerOrange else Color.Gray)
            )

            statCards.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { (label, value, color) ->
                        StatCard(
                            label = label, value = value, color = color,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // If odd row, fill remaining space
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            // ── Zone status section ───────────────────────────────────────────
            if (zoneStatusCounts.isNotEmpty()) {
                Text(
                    text = "Zone Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    items(zoneStatusCounts.entries.toList()) { (status, count) ->
                        val iconColor = zoneStatusColor(status)
                        AssistChip(
                            onClick = {},
                            label = {
                                Text("${status.replaceFirstChar { it.uppercase() }}: $count")
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = iconColor
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors()
                        )
                    }
                }
            }

            // ── Per-ranger sighting counts ────────────────────────────────────
            if (rangerSightingCounts.isNotEmpty()) {
                Text(
                    text = "Sightings by Ranger",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val maxCount = rangerSightingCounts.firstOrNull()?.count ?: 1
                        rangerSightingCounts.forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = entry.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${entry.count}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ── Last sync ─────────────────────────────────────────────────────
            lastSyncDate?.let { syncTime ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = RangerGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Last synced: ${
                            DateUtils.getRelativeTimeSpanString(
                                syncTime,
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS
                            )
                        }",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun zoneStatusColor(status: String): Color = when (status) {
    "active" -> ZoneActive
    "underTreatment" -> ZoneUnderTreatment
    "cleared" -> ZoneCleared
    else -> Color.Gray
}
