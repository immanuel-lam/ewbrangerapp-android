package org.yac.llamarangers.ui.hazard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogHazardScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: HazardLogViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Environmental") }
    var selectedSeverity by remember { mutableStateOf("Moderate") }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val hazardTypes = listOf("Environmental", "Biological", "Infrastructure", "Weather", "Other")
    val severities = listOf("Low", "Moderate", "High", "Critical")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Hazard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            isSaving = true
                            viewModel.logHazard(
                                title = title.ifBlank { "New Hazard" },
                                type = selectedType,
                                severity = selectedSeverity,
                                notes = notes.ifBlank { null },
                                photoPath = null
                            )
                            onNavigateBack()
                        },
                        enabled = !isSaving && title.isNotBlank()
                    ) {
                        Text("Save")
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
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Hazard Title") },
                placeholder = { Text("e.g. Wasp nest, Flooded creek") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Type
            Column {
                Text(
                    text = "Hazard Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    hazardTypes.take(3).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    hazardTypes.drop(3).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Severity
            Column {
                Text(
                    text = "Severity Level",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    severities.forEach { severity ->
                        val color = when (severity) {
                            "Critical" -> Color(0xFFC94040)
                            "High" -> Color(0xFFC4692A)
                            "Moderate" -> Color(0xFFEBC400)
                            else -> Color(0xFF2D6A4F)
                        }
                        FilterChip(
                            selected = selectedSeverity == severity,
                            onClick = { selectedSeverity = severity },
                            label = { Text(severity) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Additional Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            // Location Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Current GPS location will be attached automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
