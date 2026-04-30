package org.yac.llamarangers.ui.safety

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyCheckInScreen(
    viewModel: SafetyCheckInViewModel = hiltViewModel()
) {
    val isActive by viewModel.isActive.collectAsState()
    val isSOSTriggered by viewModel.isSOSTriggered.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety Check-In") }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Status Header
                SafetyStatusHeader(isActive = isActive)

                if (isActive) {
                    ActiveSafetyTimer(viewModel = viewModel)
                } else {
                    InactiveSafetySetup(viewModel = viewModel)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SOS Button (Always available but more prominent when active)
                SOSButton(
                    onClick = { viewModel.triggerSOS() },
                    isActive = isActive
                )
                
                // SOS Info
                Text(
                    text = "SOS alerts all rangers over Mesh sync and sounds a local alarm. Use in emergencies only.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            // SOS Overlay
            if (isSOSTriggered) {
                SOSOverlay(onDismiss = { viewModel.dismissSOS() })
            }
        }
    }
}

@Composable
private fun SafetyStatusHeader(isActive: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isActive) Color(0xFF2D6A4F) else MaterialTheme.colorScheme.outline,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isActive) Icons.Default.Shield else Icons.Default.ShieldMoon,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Column {
                Text(
                    text = if (isActive) "Active Safety Session" else "Safety Inactive",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isActive) "Periodic check-ins required" else "Start a session before heading out",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActiveSafetyTimer(viewModel: SafetyCheckInViewModel) {
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val intervalMinutes by viewModel.intervalMinutes.collectAsState()
    
    val progress = remainingSeconds.toFloat() / (intervalMinutes * 60)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12.dp,
                color = if (progress < 0.2f) Color(0xFFC94040) else Color(0xFF2D6A4F),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = viewModel.timeString,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp
                )
                Text(
                    text = "until check-in",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.checkIn() },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Check In", fontWeight = FontWeight.Bold)
            }
            
            OutlinedButton(
                onClick = { viewModel.stopSession() },
                modifier = Modifier.weight(0.6f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("End Session")
            }
        }
    }
}

@Composable
private fun InactiveSafetySetup(viewModel: SafetyCheckInViewModel) {
    var selectedMinutes by remember { mutableIntStateOf(60) }
    val options = listOf(30, 60, 90, 120)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Select Check-In Interval",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { mins ->
                FilterChip(
                    selected = selectedMinutes == mins,
                    onClick = { selectedMinutes = mins },
                    label = { Text("$mins min") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Button(
            onClick = { viewModel.startSafetySession(selectedMinutes) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Safety Session", fontWeight = FontWeight.Bold)
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(
                    "You will be prompted to check in at this interval. If you fail to check in, an SOS will be automatically triggered.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun SOSButton(onClick: () -> Unit, isActive: Boolean) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC94040)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Text(
                "TRIGGER SOS",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun SOSOverlay(onDismiss: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "sos")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFC94040).copy(alpha = alpha))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(120.dp)
            )
            
            Text(
                "SOS TRIGGERED",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Text(
                "Your location is being broadcast to all rangers. Nearby devices will sound an alarm.",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFC94040)),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text("CANCEL SOS", fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
        }
    }
}
