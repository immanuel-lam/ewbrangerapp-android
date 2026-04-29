package org.yac.llamarangers.ui.sighting

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import org.yac.llamarangers.domain.model.enums.InfestationSize
import org.yac.llamarangers.domain.model.enums.InvasiveSpecies
import org.yac.llamarangers.service.location.LocationManager
import org.yac.llamarangers.ui.components.LargeButton
import org.yac.llamarangers.ui.components.VariantColourDot
import org.yac.llamarangers.ui.theme.RangerGreen
import java.io.File
import java.util.UUID

import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

/**
 * Form for logging a new sighting — M3 polish pass.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSightingScreen(
    viewModel: LogSightingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val capturedLocation by viewModel.capturedLocation.collectAsState()
    val accuracyLevel by viewModel.accuracyLevel.collectAsState()
    val selectedSpecies by viewModel.selectedSpecies.collectAsState()
    val selectedSize by viewModel.selectedSize.collectAsState()
    val biocontrolObservation by viewModel.biocontrolObservation.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val photoFilenames by viewModel.photoFilenames.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveError by viewModel.saveError.collectAsState()
    val didSave by viewModel.didSave.collectAsState()
    val canSave by viewModel.canSave.collectAsState()

    LaunchedEffect(didSave) {
        if (didSave) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Sighting") },
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
                .verticalScroll(rememberScrollState())
        ) {
            // ── GPS Section ──────────────────────────────────────────────────
            SectionHeader(title = "Location")
            HorizontalDivider()
            GpsSection(
                location = capturedLocation,
                accuracyLevel = accuracyLevel,
                onRecapture = { viewModel.recaptureLocation() }
            )

            // ── Species Section ──────────────────────────────────────────────
            HorizontalDivider()
            SectionHeader(title = "Species")
            HorizontalDivider()
            VariantSection(
                selectedVariant = selectedSpecies,
                onVariantSelected = { viewModel.setSelectedSpecies(it) }
            )

            if (selectedSpecies == InvasiveSpecies.LANTANA) {
                BiocontrolPromptCard(
                    observation = biocontrolObservation,
                    onObservationSelected = { viewModel.setBiocontrolObservation(it) }
                )
            }

            // ── Size Section ─────────────────────────────────────────────────
            HorizontalDivider()
            SectionHeader(title = "Infestation Size")
            HorizontalDivider()
            SizeSection(
                selectedSize = selectedSize,
                onSizeSelected = { viewModel.setSelectedSize(it) }
            )

            // ── Photos Section ───────────────────────────────────────────────
            HorizontalDivider()
            SectionHeader(title = "Photos")
            HorizontalDivider()
            PhotoSection(
                photoFilenames = photoFilenames,
                onPhotoAdded = { viewModel.addPhoto(it) }
            )

            // ── Notes Section ────────────────────────────────────────────────
            HorizontalDivider()
            SectionHeader(title = "Notes")
            HorizontalDivider()
            NotesSection(
                notes = notes,
                onNotesChanged = { viewModel.setNotes(it) }
            )

            HorizontalDivider()

            // Error
            saveError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ── Save Button ──────────────────────────────────────────────────
            LargeButton(
                title = if (isSaving) "Saving…" else "Save Sighting",
                onClick = { viewModel.save() },
                isEnabled = canSave,
                isLoading = isSaving,
                color = RangerGreen,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
    )
}

// ── GPS ──────────────────────────────────────────────────────────────────────

@Composable
private fun GpsSection(
    location: Location?,
    accuracyLevel: LocationManager.AccuracyLevel,
    onRecapture: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (location != null) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = String.format("%.6f, %.6f", location.latitude, location.longitude),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AccuracyDot(level = accuracyLevel)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("\u00B1%.0fm", location.accuracy),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Acquiring GPS\u2026",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            AssistChip(
                onClick = onRecapture,
                label = { Text("Re-capture") }
            )
        }
    }
}

@Composable
private fun AccuracyDot(level: LocationManager.AccuracyLevel) {
    val color = when (level) {
        LocationManager.AccuracyLevel.GOOD -> Color(0xFF4CAF50)
        LocationManager.AccuracyLevel.FAIR -> Color(0xFFFFC107)
        LocationManager.AccuracyLevel.POOR -> Color(0xFFF44336)
        LocationManager.AccuracyLevel.UNKNOWN -> Color.Gray
    }
    Canvas(modifier = Modifier.size(8.dp)) {
        drawCircle(color = color)
    }
}

// ── Variant ───────────────────────────────────────────────────────────────────

@Composable
private fun VariantSection(
    selectedVariant: InvasiveSpecies?,
    onVariantSelected: (InvasiveSpecies) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(InvasiveSpecies.entries) { variant ->
            FilterChip(
                selected = selectedVariant == variant,
                onClick = { onVariantSelected(variant) },
                label = { Text(variant.displayName) },
                leadingIcon = {
                    VariantColourDot(variant = variant, size = 12.dp)
                }
            )
        }
    }
}

// ── Size ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SizeSection(
    selectedSize: InfestationSize,
    onSizeSelected: (InfestationSize) -> Unit
) {
    val sizes = InfestationSize.entries
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        sizes.forEachIndexed { index, size ->
            SegmentedButton(
                selected = selectedSize == size,
                onClick = { onSizeSelected(size) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = sizes.size),
                label = { Text(size.displayName) }
            )
        }
    }
}

// ── Notes ─────────────────────────────────────────────────────────────────────

@Composable
private fun NotesSection(
    notes: String,
    onNotesChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        placeholder = { Text("Additional observations…") },
        minLines = 3
    )
}

// ── Photos ────────────────────────────────────────────────────────────────────

@Composable
private fun PhotoSection(
    photoFilenames: List<String>,
    onPhotoAdded: (String) -> Unit
) {
    val context = LocalContext.current
    val photoDir = remember { File(context.filesDir, "Photos").also { it.mkdirs() } }
    val tempFilename = remember { "photo_${UUID.randomUUID()}.jpg" }
    val tempFile = remember { File(photoDir, tempFilename) }
    val tempUri: Uri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempFile.exists()) {
            val permanentName = "photo_${UUID.randomUUID()}.jpg"
            val permanentFile = File(photoDir, permanentName)
            tempFile.renameTo(permanentFile)
            onPhotoAdded(permanentName)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(tempUri)
    }

    fun launchCamera() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) cameraLauncher.launch(tempUri)
        else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(photoFilenames) { filename ->
            PhotoThumbCard(filename = filename)
        }
        if (photoFilenames.size < 3) {
            item {
                OutlinedCard(
                    onClick = { launchCamera() },
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add photo",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Photo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoThumbCard(filename: String) {
    val context = LocalContext.current
    val photoDir = File(context.filesDir, "Photos")
    val file = File(photoDir, filename)

    OutlinedCard(modifier = Modifier.size(80.dp)) {
        if (file.exists()) {
            val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = filename) {
                value = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeFile(file.absolutePath)
                }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        } else {
            PhotoPlaceholderContent()
        }
    }
}

@Composable
private fun PhotoPlaceholderContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
            Icons.Default.Photo,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Lantana Biocontrol Prompt ──────────────────────────────────────────────────

@Composable
fun BiocontrolPromptCard(
    observation: BiocontrolObservation,
    onObservationSelected: (BiocontrolObservation) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = Color(0xFFFFF3CD)
        )
    ) {
        VStack(spacing = 12.dp) {
            HStack(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                spacing = 8.dp
            ) {
                Icon(
                    Icons.Default.Close, // Using Close temporarily, should be an ant/bug icon but Android doesn't have a default ant icon
                    contentDescription = null,
                    tint = Color(0xFF856404),
                    modifier = Modifier.size(18.dp)
                )
                VStack(horizontalAlignment = Alignment.Start, spacing = 2.dp) {
                    Text(
                        "Lantana Bug Check",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Is Aconophora compressa (Lantana bug) present?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val options = listOf(
                    BiocontrolObservation.OBSERVED to "Observed",
                    BiocontrolObservation.NOT_OBSERVED to "Not Seen",
                    BiocontrolObservation.UNSURE to "Unsure"
                )

                options.forEach { (value, label) ->
                    Button(
                        onClick = { onObservationSelected(value) },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (observation == value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (observation == value) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (observation == BiocontrolObservation.OBSERVED) {
                HStack(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(Color(0xFFC4692A).copy(alpha = 0.08f), shape = RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .padding(bottom = 12.dp, start = 12.dp, end = 12.dp),
                    spacing = 8.dp
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFC4692A), // dsAccent
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        "Biocontrol present — consider delaying foliar spray",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFC4692A)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

// Helpers for VStack/HStack matching SwiftUI layout behavior
@Composable
private fun HStack(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    spacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable RowScope.() -> Unit
) {
    val arrangement = if (spacing > 0.dp) Arrangement.spacedBy(spacing) else horizontalArrangement
    Row(
        modifier = modifier,
        horizontalArrangement = arrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

@Composable
private fun VStack(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    spacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val arrangement = if (spacing > 0.dp) Arrangement.spacedBy(spacing) else verticalArrangement
    Column(
        modifier = modifier,
        verticalArrangement = arrangement,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

