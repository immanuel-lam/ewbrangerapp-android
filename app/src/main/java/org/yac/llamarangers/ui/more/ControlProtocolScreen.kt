package org.yac.llamarangers.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.yac.llamarangers.domain.model.CompatibilityResult
import org.yac.llamarangers.domain.model.Herbicide
import org.yac.llamarangers.domain.model.HerbicideDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlProtocolScreen(
    onNavigateBack: () -> Unit = {}
) {
    var selectedHerbicideA by remember { mutableStateOf<Herbicide?>(null) }
    var selectedHerbicideB by remember { mutableStateOf<Herbicide?>(null) }
    var showPickerA by remember { mutableStateOf(false) }
    var showPickerB by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Herbicide Checker") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Check chemical compatibility before tank-mixing herbicides.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Herbicide Selection
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HerbicideSelector(
                    label = "Herbicide 1",
                    selected = selectedHerbicideA,
                    onClick = { showPickerA = true }
                )
                
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                }

                HerbicideSelector(
                    label = "Herbicide 2",
                    selected = selectedHerbicideB,
                    onClick = { showPickerB = true }
                )
            }

            // Results Section
            if (selectedHerbicideA != null && selectedHerbicideB != null) {
                CompatibilityResultCard(
                    herbicideA = selectedHerbicideA!!,
                    herbicideB = selectedHerbicideB!!
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Science,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Select two herbicides to check compatibility",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showPickerA) {
            HerbicidePickerSheet(
                onSelect = { selectedHerbicideA = it; showPickerA = false },
                onDismiss = { showPickerA = false }
            )
        }

        if (showPickerB) {
            HerbicidePickerSheet(
                onSelect = { selectedHerbicideB = it; showPickerB = false },
                onDismiss = { showPickerB = false }
            )
        }
    }
}

@Composable
private fun HerbicideSelector(
    label: String,
    selected: Herbicide?,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = selected?.name ?: "Tap to select...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selected == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun CompatibilityResultCard(
    herbicideA: Herbicide,
    herbicideB: Herbicide
) {
    val result = HerbicideDatabase.compatibility(herbicideA, herbicideB)
    val color = when (result) {
        CompatibilityResult.COMPATIBLE -> Color(0xFF2A7A4A)
        CompatibilityResult.INCOMPATIBLE -> Color(0xFFC94040)
        CompatibilityResult.SAME_PRODUCT -> Color(0xFFC4692A)
    }
    val icon = when (result) {
        CompatibilityResult.COMPATIBLE -> Icons.Default.CheckCircle
        CompatibilityResult.INCOMPATIBLE -> Icons.Default.Cancel
        CompatibilityResult.SAME_PRODUCT -> Icons.Default.Info
    }
    val title = when (result) {
        CompatibilityResult.COMPATIBLE -> "Compatible"
        CompatibilityResult.INCOMPATIBLE -> "INCOMPATIBLE"
        CompatibilityResult.SAME_PRODUCT -> "Same Active Ingredient"
    }
    val description = when (result) {
        CompatibilityResult.COMPATIBLE -> "These chemicals can be safely mixed in a single tank application."
        CompatibilityResult.INCOMPATIBLE -> "DO NOT MIX. These chemicals may cause a precipitate or neutralise each other."
        CompatibilityResult.SAME_PRODUCT -> "Mixing these is redundant as they contain the same or similar active ingredients."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color)
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = color)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium)
            
            if (result == CompatibilityResult.INCOMPATIBLE) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = color.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Separate applications by at least 7 days to avoid soil interaction or plant stress.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HerbicidePickerSheet(
    onSelect: (Herbicide) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val herbicides = HerbicideDatabase.all

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Select Herbicide", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            herbicides.forEach { h ->
                ListItem(
                    headlineContent = { Text(h.name, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(h.activeIngredient, style = MaterialTheme.typography.bodySmall) },
                    leadingContent = { Icon(Icons.Default.Science, contentDescription = null) },
                    modifier = Modifier.clickable { onSelect(h) }
                )
                HorizontalDivider()
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
