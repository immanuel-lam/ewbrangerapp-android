package org.yac.llamarangers.ui.hub

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.data.local.entity.RangerProfileEntity
import org.yac.llamarangers.domain.model.enums.RangerRole
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubScreen(
    ranger: RangerProfileEntity?,
    onLogout: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToSupplies: () -> Unit = {},
    onNavigateToDaySync: () -> Unit = {},
    onNavigateToZones: () -> Unit = {},
    onNavigateToCloudSync: () -> Unit = {},
    onNavigateToHandover: () -> Unit = {},
    onNavigateToEquipment: () -> Unit = {},
    onNavigateToHazards: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    var tilesAppeared by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!tilesAppeared) {
            delay(50)
            tilesAppeared = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Hub") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile card
            HubProfileCard(
                ranger = ranger,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )

            // Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp) // Static height for scroll view inside scroll view workaround
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                item {
                    HubTile(
                        title = "Dashboard", subtitle = "Stats & progress",
                        icon = "bar_chart", accent = Color(0xFF2D6A4F), appeared = tilesAppeared,
                        onClick = onNavigateToDashboard
                    )
                }
                item {
                    HubTile(
                        title = "Supplies", subtitle = "Herbicide stock",
                        icon = "science", accent = Color(0xFFC4692A), appeared = tilesAppeared,
                        onClick = onNavigateToSupplies
                    )
                }
                item {
                    HubTile(
                        title = "Day Sync", subtitle = "Mesh sync devices",
                        icon = "cell_tower", accent = Color(0xFF2E7A6B), appeared = tilesAppeared,
                        onClick = onNavigateToDaySync
                    )
                }
                item {
                    HubTile(
                        title = "Zones", subtitle = "Manage areas",
                        icon = "grid_view", accent = Color(0xFF7B5EA8), appeared = tilesAppeared,
                        onClick = onNavigateToZones
                    )
                }
                item {
                    HubTile(
                        title = "Cloud Sync", subtitle = "Supabase · S3",
                        icon = "cloud", accent = Color(0xFF3ECF8E), appeared = tilesAppeared,
                        onClick = onNavigateToCloudSync
                    )
                }
                item {
                    HubTile(
                        title = "Handover", subtitle = "End of shift report",
                        icon = "description", accent = Color(0xFF8B5E3C), appeared = tilesAppeared,
                        onClick = onNavigateToHandover
                    )
                }
                item {
                    HubTile(
                        title = "Equipment", subtitle = "Maintenance logs",
                        icon = "build", accent = Color(0xFF8B5E3C), appeared = tilesAppeared,
                        onClick = onNavigateToEquipment
                    )
                }
                item {
                    HubTile(
                        title = "Hazards", subtitle = "Log field hazards",
                        icon = "warning", accent = Color(0xFFC94040), appeared = tilesAppeared,
                        onClick = onNavigateToHazards
                    )
                }
            }

            // Settings link
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .clickable { onNavigateToSettings() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Sign off
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 48.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Sign Off", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun HubProfileCard(ranger: RangerProfileEntity?, modifier: Modifier = Modifier) {
    val name = ranger?.displayName ?: "Ranger"
    val parts = name.split(" ")
    val initials = parts.take(2).mapNotNull { it.firstOrNull()?.toString() }.joinToString("")
    
    val roleLabel = RangerRole.fromValue(ranger?.role ?: "ranger").displayName
    val avatarColor = Color(0xFF2D6A4F)

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Name + role
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = roleLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // YAC badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .background(Color(0xFF2D6A4F).copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.Spa,
                    contentDescription = null,
                    tint = Color(0xFF2D6A4F),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "YAC",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2D6A4F)
                )
            }
        }
    }
}

@Composable
private fun HubTile(
    title: String,
    subtitle: String,
    icon: String,
    accent: Color,
    appeared: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.9f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 50f),
        label = "scale"
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .scale(scale)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    getIconByName(icon),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getIconByName(name: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when(name) {
        "bar_chart" -> Icons.Default.BarChart
        "science" -> Icons.Default.Science
        "cell_tower" -> Icons.Default.CellTower
        "grid_view" -> Icons.Default.GridView
        "cloud" -> Icons.Default.Cloud
        "description" -> Icons.Default.Description
        "build" -> Icons.Default.Build
        "warning" -> Icons.Default.Warning
        else -> Icons.Default.Circle
    }
}