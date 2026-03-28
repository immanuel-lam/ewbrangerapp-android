package org.yac.llamarangers.ui.sighting

import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import org.yac.llamarangers.domain.model.enums.LantanaVariant
import org.yac.llamarangers.service.location.LocationManager
import org.yac.llamarangers.ui.components.LargeButton
import org.yac.llamarangers.ui.components.VariantColourDot
import org.yac.llamarangers.ui.theme.RangerGreen
import java.io.File
import java.util.UUID

/**
 * Form for logging a new sighting — M3 polish pass.
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

            // ── Variant Section ──────────────────────────────────────────────
            HorizontalDivider()
            SectionHeader(title = "Variant")
            HorizontalDivider()
            VariantSection(
                selectedVariant = selectedVariant,
                onVariantSelected = { viewModel.setSelectedVariant(it) }
            )

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
                isEnabled = viewModel.canSave,
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
    selectedVariant: LantanaVariant?,
    onVariantSelected: (LantanaVariant) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(LantanaVariant.entries) { variant ->
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
                    onClick = { cameraLauncher.launch(tempUri) },
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
