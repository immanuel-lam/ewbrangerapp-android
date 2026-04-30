package org.yac.llamarangers.ui.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.domain.model.enums.InvasiveSpecies
import org.yac.llamarangers.domain.model.enums.TreatmentMethod
import org.yac.llamarangers.resources.InvasiveSpeciesContent
import org.yac.llamarangers.ui.components.VariantColourDot

/**
 * Species identification detail screen.
 * Ports iOS SpeciesDetailView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeciesDetailScreen(
    speciesValue: String,
    onNavigateBack: () -> Unit = {}
) {
    val species = InvasiveSpecies.fromValue(speciesValue)
    val info = InvasiveSpeciesContent.infoFor(species)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(species.displayName) },
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
            // Header: Name + Scientific Name
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VariantColourDot(variant = species, size = 24.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = species.displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = species.scientificName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            // Distinguishing Features
            SectionCard(title = "Identification", icon = Icons.Default.Search) {
                Text(
                    text = info?.distinguishingFeatures ?: species.distinguishingFeatures,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Control Methods
            SectionCard(title = "Control Methods", icon = Icons.Default.Construction) {
                species.controlMethods.forEach { method ->
                    ControlMethodRow(method = method)
                    if (method != species.controlMethods.last()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }

            // Seasonal Notes
            info?.seasonalNotes?.let { notes ->
                SectionCard(title = "Seasonal Notes", icon = Icons.Default.CalendarToday) {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Biocontrol
            if (species.hasBiocontrolConcern) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Biocontrol Insects",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Lantana bugs may be present. If observed, avoid using foliar spray as it may harm these helpful insects.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ControlMethodRow(method: TreatmentMethod) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                treatmentMethodIcon(method),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = method.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = method.instructions,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun treatmentMethodIcon(method: TreatmentMethod): ImageVector = when (method) {
    TreatmentMethod.CUT_STUMP -> Icons.Default.ContentCut
    TreatmentMethod.SPLAT_GUN -> Icons.Default.CropSquare
    TreatmentMethod.FOLIAR_SPRAY -> Icons.Default.WaterDrop
    TreatmentMethod.BASAL_BARK -> Icons.Default.Park
    TreatmentMethod.MECHANICAL -> Icons.Default.BackHand
    TreatmentMethod.STEM_INJECTION -> Icons.Default.Vaccines
    TreatmentMethod.FIRE_MANAGEMENT -> Icons.Default.LocalFireDepartment
}
