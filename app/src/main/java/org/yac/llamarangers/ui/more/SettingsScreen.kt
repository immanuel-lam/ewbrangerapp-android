package org.yac.llamarangers.ui.more

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.yac.llamarangers.service.map.OfflineTileManager
import org.yac.llamarangers.ui.theme.RangerOrange
import org.yac.llamarangers.ui.theme.RangerRed

/**
 * Settings screen.
 * Ports iOS SettingsView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentRangerName by viewModel.currentRangerName.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val lastSyncDate by viewModel.lastSyncDate.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val recentRainFlagged by viewModel.recentRainFlagged.collectAsStateWithLifecycle()
    val tileStatus by viewModel.tileStatus.collectAsStateWithLifecycle()

    var showEditName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var showChangePIN by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
        ) {
            // Ranger Profile section
            SectionHeader("Ranger Profile")
            SettingsRow("Name", currentRangerName.ifEmpty { "\u2014" })
            TextButton(
                onClick = {
                    editedName = currentRangerName
                    showEditName = true
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) { Text("Edit Name") }
            TextButton(
                onClick = { showChangePIN = true },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) { Text("Change PIN") }

            HorizontalDivider()

            // Sync section
            SectionHeader("Sync")
            SettingsRow(
                "Pending Records",
                "$pendingSyncCount",
                valueColor = if (pendingSyncCount > 0) RangerOrange else null
            )
            lastSyncDate?.let { syncTime ->
                SettingsRow(
                    "Last Synced",
                    DateUtils.getRelativeTimeSpanString(
                        syncTime,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    ).toString()
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { viewModel.syncNow() },
                    enabled = !isSyncing
                ) { Text("Sync Now") }
                if (isSyncing) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(16.dp)
                            .width(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            HorizontalDivider()

            // Field Conditions section
            SectionHeader("Field Conditions")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Rain Event")
                Switch(
                    checked = recentRainFlagged,
                    onCheckedChange = { viewModel.toggleRecentRain() }
                )
            }

            HorizontalDivider()

            // Offline Maps section
            SectionHeader("Offline Maps")
            when (val status = tileStatus) {
                is OfflineTileManager.TileStatus.Available -> {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Tiles Available (${status.version})")
                        Text(
                            status.coverage,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is OfflineTileManager.TileStatus.Unavailable -> {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = RangerRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tiles not downloaded", color = RangerRed)
                    }
                }
                is OfflineTileManager.TileStatus.Downloading -> {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Downloading...")
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { status.progress.toFloat() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is OfflineTileManager.TileStatus.Checking -> {
                    Text(
                        "Checking...",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            HorizontalDivider()

            // App section
            SectionHeader("App")
            SettingsRow("Version", viewModel.appVersion)
            TextButton(
                onClick = {
                    viewModel.logout()
                    onLoggedOut()
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text("Logout", color = RangerRed)
            }
        }
    }

    // Edit Name dialog
    if (showEditName) {
        AlertDialog(
            onDismissRequest = { showEditName = false },
            title = { Text("Edit Name") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Display name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateDisplayName(editedName)
                    showEditName = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditName = false }) { Text("Cancel") }
            }
        )
    }

    // Change PIN bottom sheet
    if (showChangePIN) {
        ChangePINBottomSheet(
            viewModel = viewModel,
            onDismiss = { showChangePIN = false }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            value,
            color = valueColor ?: MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePINBottomSheet(
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val pinChangeError by viewModel.pinChangeError.collectAsStateWithLifecycle()
    val pinChangeSuccess by viewModel.pinChangeSuccess.collectAsStateWithLifecycle()

    var oldPIN by remember { mutableStateOf("") }
    var newPIN by remember { mutableStateOf("") }
    var confirmPIN by remember { mutableStateOf("") }

    if (pinChangeSuccess) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Change PIN", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = oldPIN,
                onValueChange = { oldPIN = it },
                label = { Text("Current PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newPIN,
                onValueChange = { newPIN = it },
                label = { Text("New PIN (min 4 digits)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirmPIN,
                onValueChange = { confirmPIN = it },
                label = { Text("Confirm New PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            pinChangeError?.let { error ->
                Text(
                    text = error,
                    color = RangerRed,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                }) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        viewModel.changePIN(oldPIN, newPIN, confirmPIN)
                    },
                    enabled = oldPIN.isNotEmpty() && newPIN.isNotEmpty() && confirmPIN.isNotEmpty()
                ) { Text("Save") }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
