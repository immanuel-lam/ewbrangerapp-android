package org.yac.llamarangers.ui.sighting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun TreatmentFollowUpScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: TreatmentFollowUpViewModel = hiltViewModel()
) {
    var percentDead by remember { mutableFloatStateOf(50f) }
    var selectedRegrowth by remember { mutableStateOf("None") }
    var notes by remember { mutableStateOf("") }
    
    val regrowthLevels = listOf("None", "Minor", "Moderate", "Heavy")
    val didSave by viewModel.didSave.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    LaunchedEffect(didSave) {
        if (didSave) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Follow-Up Survey") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveFollowUp(
                                percentDead = percentDead.toDouble(),
                                regrowthLevel = selectedRegrowth,
                                notes = notes.ifBlank { null }
                            )
                        },
                        enabled = !isSaving
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Effectiveness
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Kill Effectiveness", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${percentDead.toInt()}% dead",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Slider(
                        value = percentDead,
                        onValueChange = { percentDead = it },
                        valueRange = 0f..100f,
                        steps = 9
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("0%", style = MaterialTheme.typography.labelSmall)
                        Text("50%", style = MaterialTheme.typography.labelSmall)
                        Text("100%", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Regrowth
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Regrowth Level", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        regrowthLevels.forEach { level ->
                            FilterChip(
                                selected = selectedRegrowth == level,
                                onClick = { selectedRegrowth = level },
                                label = { Text(level) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Observation Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )
        }
    }
}
