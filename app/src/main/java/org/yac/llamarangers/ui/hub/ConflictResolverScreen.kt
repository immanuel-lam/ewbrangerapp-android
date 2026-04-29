package org.yac.llamarangers.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictResolverScreen(
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zone Conflicts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC94040))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("3 conflicts need resolution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Zone boundaries edited offline by multiple rangers", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            // Info bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Text("Conflict detected during Day Sync · LWW disabled for zone boundaries", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            // Conflicts List
            val conflicts = listOf(
                "Southern Scrub Belt" to ("24500 m²" to "24620 m²"),
                "Creek Line East" to ("18300 m²" to "18420 m²"),
                "Riparian Buffer" to ("31200 m²" to "31100 m²")
            )

            conflicts.forEach { (zoneName, areas) ->
                var resolved by remember { mutableStateOf(false) }

                if (resolved) {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2A7A4A))
                            Column {
                                Text("Resolved: $zoneName", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SyncProblem, contentDescription = null, tint = Color(0xFFC4692A))
                                Text(zoneName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider()

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(modifier = Modifier.weight(1f).background(Color(0xFFF7F3EC), RoundedCornerShape(8.dp)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Your Version", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF2D6A4F), modifier = Modifier.size(16.dp))
                                        Text("Current Ranger", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                    Text(areas.first, style = MaterialTheme.typography.bodySmall)
                                }
                                Column(modifier = Modifier.weight(1f).background(Color(0xFFF7F3EC), RoundedCornerShape(8.dp)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Peer Version", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF2D6A4F), modifier = Modifier.size(16.dp))
                                        Text("Bob Smith", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                    Text(areas.second, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            HorizontalDivider()

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { resolved = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Keep Mine")
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { resolved = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC4692A)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Keep Theirs")
                                    }
                                    Button(
                                        onClick = { resolved = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.5f), contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Merge")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
