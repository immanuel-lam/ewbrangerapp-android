package org.yac.llamarangers.ui.sighting

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.yac.llamarangers.domain.model.enums.InfestationSize
import org.yac.llamarangers.domain.model.enums.InvasiveSpecies
import org.yac.llamarangers.service.location.LocationManager
import org.yac.llamarangers.ui.components.LargeButton
import org.yac.llamarangers.ui.components.VariantColourDot
import org.yac.llamarangers.ui.theme.RangerGreen
import java.io.File
import java.util.*

/**
 * Screen for logging a new weed sighting.
 * Ports iOS LogSightingView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSightingScreen(
    viewModel: LogSightingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val capturedLocation by viewModel.capturedLocation.collectAsState()
    val accuracyLevel by viewModel.accuracyLevel.collectAsState()
    val selectedSpecies by viewModel.selectedSpecies.collectAsState()
    val selectedSize by viewModel.selectedSize.collectAsState()
    val areaEstimate by viewModel.areaEstimate.collectAsState()
    val voiceNotePath by viewModel.voiceNotePath.collectAsState()
    val biocontrolObservation by viewModel.biocontrolObservation.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val photoFilenames by viewModel.photoFilenames.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveError by viewModel.saveError.collectAsState()
    val didSave by viewModel.didSave.collectAsState()
    val canSave by viewModel.canSave.collectAsState()

    var estimationBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

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
        Box(modifier = Modifier.fillMaxSize()) {
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

                // ── Phenology Alert Section ──────────────────────────────────────
                var dismissedAlertForSpecies by remember { mutableStateOf<InvasiveSpecies?>(null) }
                val currentMonth = remember { Calendar.getInstance().get(Calendar.MONTH) + 1 }
                val alert = selectedSpecies?.let { org.yac.llamarangers.domain.model.PhenologyAlertStore.alert(it, currentMonth) }

                if (alert != null && dismissedAlertForSpecies != selectedSpecies) {
                    PhenologyAlertBanner(
                        alert = alert,
                        onDismiss = { dismissedAlertForSpecies = selectedSpecies }
                    )
                }

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
                
                areaEstimate?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = RangerGreen.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Straighten, contentDescription = null, tint = RangerGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Calculated Area", style = MaterialTheme.typography.labelSmall, color = RangerGreen)
                                Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RangerGreen)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.setAreaEstimate(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

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
                    onPhotoAdded = { viewModel.addPhoto(it) },
                    onPhotoTap = { filename ->
                        val file = File(File(context.filesDir, "Photos"), filename)
                        if (file.exists()) {
                            estimationBitmap = BitmapFactory.decodeFile(file.absolutePath)
                        }
                    }
                )

                // ── Notes Section ────────────────────────────────────────────────
                HorizontalDivider()
                SectionHeader(title = "Notes")
                HorizontalDivider()
                NotesSection(
                    notes = notes,
                    onNotesChanged = { viewModel.setNotes(it) }
                )
                
                // ── Voice Note Section ───────────────────────────────────────────
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    org.yac.llamarangers.ui.components.VoiceNoteRecorder(
                        audioFilePath = voiceNotePath,
                        onFilePathChanged = { viewModel.setVoiceNotePath(it) }
                    )
                }

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

            // Size Estimation Overlay
            estimationBitmap?.let { bitmap ->
                org.yac.llamarangers.ui.components.SizeEstimationOverlay(
                    bitmap = bitmap,
                    onConfirm = { 
                        viewModel.setAreaEstimate(it)
                        estimationBitmap = null
                    },
                    onDismiss = { estimationBitmap = null }
                )
            }
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
    onPhotoAdded: (String) -> Unit,
    onPhotoTap: (String) -> Unit = {}
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
            PhotoThumbCard(filename = filename, onClick = { onPhotoTap(filename) })
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
private fun PhotoThumbCard(filename: String, onClick: () -> Unit = {}) {
    val context = LocalContext.current
    val photoDir = File(context.filesDir, "Photos")
    val file = File(photoDir, filename)

    OutlinedCard(modifier = Modifier.size(80.dp), onClick = onClick) {
        if (file.exists()) {
            val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = filename) {
                value = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeFile(file.absolutePath)
                }
            }
            if (bitmap != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Estimate indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Straighten,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
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
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.BugReport,
                    contentDescription = null,
                    tint = Color(0xFF856404),
                    modifier = Modifier.size(18.dp)
                )
                Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                modifier = Modifier.fillMaxWidth(),
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
                Row(modifier = Modifier.padding(12.dp).background(Color(0xFFC4692A).copy(alpha = 0.08f), RoundedCornerShape(10.dp)).fillMaxWidth(), 
                    horizontalArrangement = Arrangement.spacedBy(8.dp), 
                    verticalAlignment = Alignment.CenterVertically) {
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
            }
        }
    }
}

// ── Phenology Alert Banner ────────────────────────────────────────────────────

@Composable
fun PhenologyAlertBanner(
    alert: org.yac.llamarangers.domain.model.PhenologyAlert,
    onDismiss: () -> Unit
) {
    val urgencyColor = when (alert.urgencyLevel) {
        org.yac.llamarangers.domain.model.UrgencyLevel.URGENT -> Color(0xFFC94040)
        org.yac.llamarangers.domain.model.UrgencyLevel.PRIORITY -> Color(0xFFC4692A)
        org.yac.llamarangers.domain.model.UrgencyLevel.ROUTINE -> Color(0xFF2A7A4A)
    }

    val urgencySoftColor = when (alert.urgencyLevel) {
        org.yac.llamarangers.domain.model.UrgencyLevel.URGENT -> Color(0xFFFAE8E8)
        org.yac.llamarangers.domain.model.UrgencyLevel.PRIORITY -> Color(0xFFF2DEC8)
        org.yac.llamarangers.domain.model.UrgencyLevel.ROUTINE -> Color(0xFFDAEEE3)
    }

    val urgencyIcon = when (alert.urgencyLevel) {
        org.yac.llamarangers.domain.model.UrgencyLevel.URGENT -> Icons.Default.Warning
        org.yac.llamarangers.domain.model.UrgencyLevel.PRIORITY -> Icons.Default.CalendarToday
        org.yac.llamarangers.domain.model.UrgencyLevel.ROUTINE -> Icons.Default.Spa
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = urgencySoftColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                urgencyIcon,
                contentDescription = null,
                tint = urgencyColor,
                modifier = Modifier.size(20.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alert.phase,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Box(
                        modifier = Modifier
                            .background(urgencyColor.copy(alpha = 0.15f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = alert.urgencyLevel.value,
                            style = MaterialTheme.typography.labelSmall,
                            color = urgencyColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = alert.actionRecommended,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
