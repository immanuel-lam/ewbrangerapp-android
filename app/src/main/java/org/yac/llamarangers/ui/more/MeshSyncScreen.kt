package org.yac.llamarangers.ui.more

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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

/**
 * End of Day Sync screen with animated status banner — M3 polish pass.
 * Ports iOS DemoMeshSyncView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeshSyncScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: MeshSyncViewModel = hiltViewModel()
) {
    val phase by viewModel.phase.collectAsStateWithLifecycle()
    val peers by viewModel.peers.collectAsStateWithLifecycle()
    val showPeers by viewModel.showPeers.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()

    // Animated banner colour: primary=idle, tertiary=discovering, secondary=syncing, green=done
    val targetBannerColor = when (phase) {
        SyncPhase.IDLE -> MaterialTheme.colorScheme.primaryContainer
        SyncPhase.DISCOVERING -> MaterialTheme.colorScheme.tertiaryContainer
        SyncPhase.SYNCING -> MaterialTheme.colorScheme.secondaryContainer
        SyncPhase.DONE -> RangerGreen.copy(alpha = 0.15f)
    }
    val animatedBannerColor by animateColorAsState(
        targetValue = targetBannerColor,
        animationSpec = tween(durationMillis = 600),
        label = "bannerColor"
    )

    val bannerTextColor = when (phase) {
        SyncPhase.IDLE -> MaterialTheme.colorScheme.onPrimaryContainer
        SyncPhase.DISCOVERING -> MaterialTheme.colorScheme.onTertiaryContainer
        SyncPhase.SYNCING -> MaterialTheme.colorScheme.onSecondaryContainer
        SyncPhase.DONE -> RangerGreen
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("End of Day Sync") },
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
                .padding(16.dp)
        ) {
            // ── Animated status banner ────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = animatedBannerColor,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = viewModel.bannerText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = bannerTextColor,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Peer rows ─────────────────────────────────────────────────────
            AnimatedVisibility(visible = showPeers, enter = slideInVertically() + fadeIn()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Nearby Rangers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    peers.forEach { peer ->
                        PeerCard(peer = peer)
                    }
                }
            }

            if (!showPeers) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CellTower,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tap Start Sync to find nearby rangers",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // ── Summary ───────────────────────────────────────────────────────
            summary?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            // ── Buttons ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (phase == SyncPhase.IDLE || phase == SyncPhase.DONE) {
                    Button(
                        onClick = { viewModel.onButtonTap() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (phase == SyncPhase.DONE) RangerGreen
                            else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(viewModel.buttonTitle)
                    }
                } else {
                    // Discovering or Syncing: show disabled Start + active Stop
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        enabled = false
                    ) {
                        Text(viewModel.buttonTitle)
                    }
                    OutlinedButton(
                        onClick = { /* stop not wired in demo */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stop")
                    }
                }
            }
        }
    }
}

@Composable
private fun PeerCard(peer: PeerInfo) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = peer.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = peer.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (peer.progress >= 1.0) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Complete",
                        tint = RangerGreen
                    )
                }
            }
            if (peer.progress > 0 && peer.progress < 1.0) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { peer.progress.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
