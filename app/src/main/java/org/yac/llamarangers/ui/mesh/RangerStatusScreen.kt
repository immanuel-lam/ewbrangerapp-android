package org.yac.llamarangers.ui.mesh

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangerStatusScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: RangerStatusViewModel = hiltViewModel()
) {
    val myStatus by viewModel.myStatus.collectAsState()
    val nearbyRangers by viewModel.nearbyRangers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ranger Status") },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // My Status Card
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("My Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider()
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Current Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        val statuses = listOf("On Patrol", "Resting", "Heading Back")
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            statuses.forEachIndexed { index, status ->
                                SegmentedButton(
                                    selected = myStatus == status,
                                    onClick = { viewModel.setMyStatus(status) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = statuses.size)
                                ) { Text(status, style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                    }
                }
            }

            // Assistance Button
            Button(
                onClick = { viewModel.setMyStatus("Needs Assistance") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC94040)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Text("Need Assistance", fontWeight = FontWeight.Bold)
                }
            }

            // Nearby Rangers
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CellTower, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nearby Rangers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${nearbyRangers.size}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (nearbyRangers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No rangers in range", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    nearbyRangers.forEach { ranger ->
                        RangerStatusRow(ranger = ranger)
                    }
                }
            }
        }
    }
}

@Composable
private fun RangerStatusRow(ranger: RangerStatus) {
    val minsAgo = (System.currentTimeMillis() - ranger.lastSeen) / 60000
    val presenceColor = when {
        minsAgo < 2 -> Color(0xFF2D6A4F)
        minsAgo < 5 -> Color(0xFFC4692A)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(10.dp).background(presenceColor, CircleShape))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(ranger.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                ranger.zone?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = ranger.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (ranger.status == "Needs Assistance") Color(0xFFC94040) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (ranger.status == "Needs Assistance") FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = if (minsAgo < 1) "just now" else "${minsAgo}m ago",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
