package org.yac.llamarangers.ui.more

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.yac.llamarangers.ui.theme.RangerBlue
import org.yac.llamarangers.ui.theme.RangerGreen
import org.yac.llamarangers.ui.theme.RangerOrange
import org.yac.llamarangers.ui.theme.RangerRed
import org.yac.llamarangers.ui.theme.ZoneActive
import org.yac.llamarangers.ui.theme.ZoneCleared
import org.yac.llamarangers.ui.theme.ZoneUnderTreatment

/**
 * Dashboard screen showing aggregated statistics.
 * Ports iOS DashboardView.
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
            // Sync status banner
            if (pendingSyncCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RangerOrange.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
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

            // Stat cards grid
            val statCards = listOf(
                StatCardData("Total\nSightings", "$totalSightings", RangerRed),
                StatCardData("This\nMonth", "$sightingsThisMonth", RangerOrange),
                StatCardData("Treatments\nThis Month", "$treatmentsThisMonth", RangerBlue),
                StatCardData("Zones\nCleared", "${clearedZonePercent.toInt()}%", RangerGreen),
                StatCardData(
                    "Open\nFollow-ups",
                    "$openFollowUpTasks",
                    if (openFollowUpTasks > 0) RangerOrange else Color.Gray
                )
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(220.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(statCards) { card ->
                    StatCard(title = card.title, value = card.value, color = card.color)
                }
            }

            // Zone status section
            if (zoneStatusCounts.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text("Zones by Status", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        zoneStatusCounts.forEach { (status, count) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(zoneStatusColor(status)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = status.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Per-ranger sighting counts
            if (rangerSightingCounts.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Sightings by Ranger", style = MaterialTheme.typography.titleMedium)
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.width(80.dp)) {
                                val fraction = if (maxCount > 0) entry.count.toFloat() / maxCount else 0f
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction.coerceAtLeast(0.05f))
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(RangerRed.copy(alpha = 0.7f))
                                )
                            }
                        }
                    }
                }
            }

            // Last sync
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

private data class StatCardData(val title: String, val value: String, val color: Color)

@Composable
private fun StatCard(title: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun zoneStatusColor(status: String): Color = when (status) {
    "active" -> ZoneActive
    "underTreatment" -> ZoneUnderTreatment
    "cleared" -> ZoneCleared
    else -> Color.Gray
}
