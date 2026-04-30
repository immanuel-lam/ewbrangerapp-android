package org.yac.llamarangers.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoLiveSyncScreen(
    onNavigateBack: () -> Unit = {}
) {
    var isSyncing by remember { mutableStateOf(false) }
    var syncProgress by remember { mutableFloatStateOf(0f) }
    var syncStatus by remember { mutableStateOf("Ready to sync") }
    var logLines by remember { mutableStateOf(listOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cloud Sync") },
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
            // Status banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (isSyncing) "Sync in progress" else "Not connected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        syncStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF3ECF8E), strokeWidth = 3.dp)
                } else {
                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(if(syncProgress == 1f) Color(0xFF2D6A4F) else Color.Gray))
                }
            }

            // Sync Database Card
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF3ECF8E))
                        Text("Database Tables", style = MaterialTheme.typography.titleMedium)
                    }
                    
                    val tables = listOf("sighting_logs" to 28, "treatment_records" to 18, "patrol_records" to 10, "ranger_tasks" to 7, "infestation_zones" to 6)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        tables.forEach { (name, total) ->
                            val current = if (isSyncing && syncProgress > 0.1f) (total * syncProgress).toInt() else if (syncProgress == 1f) total else 0
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(name, style = MaterialTheme.typography.bodyMedium, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("$current/$total", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            LinearProgressIndicator(
                                progress = { if (isSyncing || syncProgress == 1f) syncProgress else 0f },
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF3ECF8E),
                                trackColor = Color.LightGray.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
            
            // Sync Photo Card
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cloud, contentDescription = null, tint = Color(0xFF3ECF8E))
                        Text("Backup & Storage", style = MaterialTheme.typography.titleMedium)
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Supabase Storage", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        val count = if (isSyncing && syncProgress > 0.1f) (28 * syncProgress).toInt() else if (syncProgress == 1f) 28 else 0
                        Text("$count / 28", style = MaterialTheme.typography.bodySmall, color = Color(0xFF3ECF8E), fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { if (isSyncing || syncProgress == 1f) syncProgress else 0f },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF3ECF8E),
                        trackColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("S3 Replica", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        val s3Count = if (isSyncing && syncProgress > 0.2f) (28 * (syncProgress - 0.1f)).toInt() else if (syncProgress == 1f) 28 else 0
                        Text("$s3Count / 28", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF9900), fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { if (isSyncing || syncProgress == 1f) maxOf(0f, syncProgress - 0.1f) else 0f },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFF9900),
                        trackColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                }
            }

            // Start Sync Button
            Button(
                onClick = {
                    if (isSyncing) return@Button
                    isSyncing = true
                    syncProgress = 0f
                    logLines = emptyList()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3ECF8E))
            ) {
                Text(if (isSyncing) "Syncing..." else "Sync to Cloud", fontWeight = FontWeight.Bold, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    LaunchedEffect(isSyncing) {
        if (isSyncing) {
            syncStatus = "Connecting to Supabase..."
            logLines = logLines + "Resolving yac-rangers.supabase.co..."
            delay(800)
            syncStatus = "Syncing..."
            logLines = logLines + "Supabase latency=45ms tls=1.3"
            logLines = logLines + "BEGIN TRANSACTION"
            for (i in 1..10) {
                delay(400)
                syncProgress = i / 10f
            }
            logLines = logLines + "COMMIT"
            logLines = logLines + "Sync complete 69 rows"
            syncStatus = "Sync complete"
            isSyncing = false
        }
    }
}