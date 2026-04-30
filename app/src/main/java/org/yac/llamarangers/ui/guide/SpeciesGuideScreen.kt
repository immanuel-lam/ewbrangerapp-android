package org.yac.llamarangers.ui.guide

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.domain.model.enums.InvasiveSpecies
import org.yac.llamarangers.ui.components.VariantColourDot

/**
 * Species identification guide list screen.
 * Ports iOS SpeciesGuideView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeciesGuideScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToDetail: (speciesValue: String) -> Unit = {}
) {
    val speciesList = InvasiveSpecies.entries.filter { it != InvasiveSpecies.UNKNOWN }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Species Guide") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(speciesList) { species ->
                SpeciesGuideCard(
                    species = species,
                    onClick = { onNavigateToDetail(species.value) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SpeciesGuideCard(
    species: InvasiveSpecies,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card header: colour dot + name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                VariantColourDot(variant = species, size = 20.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = species.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = species.scientificName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            // Control method chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                species.controlMethods.take(3).forEach { method ->
                    AssistChip(
                        onClick = {},
                        label = { Text(method.displayName, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(24.dp)
                    )
                }
                if (species.controlMethods.size > 3) {
                    Text(
                        text = "+${species.controlMethods.size - 3}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp)
                    )
                }
            }

            // Biocontrol warning row
            if (species.hasBiocontrolConcern) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Biocontrol Concern",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
