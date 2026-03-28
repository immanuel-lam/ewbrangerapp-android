package org.yac.llamarangers.ui.sighting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.yac.llamarangers.ui.components.LargeButton
import org.yac.llamarangers.ui.theme.RangerGreen

/**
 * Form for logging a new sighting.
 * Ports iOS LogSightingView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSightingScreen(
    viewModel: LogSightingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val capturedLocation by viewModel.capturedLocation.collectAsState()
    val accuracyLevel by viewModel.accuracyLevel.collectAsState()
    val selectedVariant by viewModel.selectedVariant.collectAsState()
    val selectedSize by viewModel.selectedSize.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val photoFilenames by viewModel.photoFilenames.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveError by viewModel.saveError.collectAsState()
    val didSave by viewModel.didSave.collectAsState()

    LaunchedEffect(didSave) {
        if (didSave) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Sighting") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
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
                .verticalScroll(rememberScrollState())
        ) {
            // GPS capture
            GPSCaptureView(
                location = capturedLocation,
                accuracyLevel = accuracyLevel,
                onRecapture = { viewModel.recaptureLocation() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Variant picker
            VariantPicker(
                selectedVariant = selectedVariant,
                onVariantSelected = { viewModel.setSelectedVariant(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Size picker
            SizePicker(
                selectedSize = selectedSize,
                onSizeSelected = { viewModel.setSelectedSize(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Photo capture
            PhotoCaptureView(
                photoFilenames = photoFilenames,
                onPhotoAdded = { viewModel.addPhoto(it) }
            )

            // Control recommendation
            viewModel.controlRecommendation?.let { rec ->
                Spacer(modifier = Modifier.height(20.dp))
                ControlRecommendationView(recommendation = rec)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Notes
            NotesField(
                notes = notes,
                onNotesChanged = { viewModel.setNotes(it) }
            )

            // Error
            saveError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            LargeButton(
                title = "Save Sighting",
                onClick = { viewModel.save() },
                isEnabled = viewModel.canSave,
                isLoading = isSaving,
                color = RangerGreen
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun NotesField(
    notes: String,
    onNotesChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "Notes (optional)",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.material3.OutlinedTextField(
            value = notes,
            onValueChange = onNotesChanged,
            modifier = Modifier
                .fillMaxSize()
                .height(100.dp),
            placeholder = { Text("Additional observations...") }
        )
    }
}
