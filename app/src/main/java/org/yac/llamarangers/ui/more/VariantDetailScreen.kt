package org.yac.llamarangers.ui.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.yac.llamarangers.domain.model.SeasonalAlert
import org.yac.llamarangers.domain.model.enums.InvasiveSpecies
import org.yac.llamarangers.domain.model.enums.TreatmentMethod
import org.yac.llamarangers.ui.components.SeasonalAlertBanner
import org.yac.llamarangers.ui.theme.RangerGreen

/**
 * Variant detail screen — M3 polish pass.
 * Ports iOS VariantDetailView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariantDetailScreen(
    variantValue: String,
    onNavigateBack: () -> Unit = {}
) {
    val variant = InvasiveSpecies.fromValue(variantValue)
    val headerTextColor = if (variant.color.luminance() > 0.65f) Color.Black else Color.White
    val variantColor = variant.color

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(variant.displayName) },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Full-width header Box (160dp) ─────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .drawBehind {
                        drawRect(
                            color = variantColor,
                            topLeft = Offset.Zero,
                            size = size
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = variant.displayName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.4f),
                                offset = Offset(1f, 1f),
                                blurRadius = 4f
                            )
                        ),
                        color = headerTextColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lantana camara var.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = headerTextColor.copy(alpha = 0.7f)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Biocontrol warning ────────────────────────────────────────
                if (variant.hasBiocontrolConcern) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        SeasonalAlertBanner(
                            alert = SeasonalAlert(
                                title = "Check for Biocontrol Insects",
                                message = "During the wet season (Nov\u2013Mar), check for lantana bug before applying chemicals to pink Lantana.",
                                severity = SeasonalAlert.Severity.WARNING
                            )
                        )
                    }
                }

                // ── Identifying features ──────────────────────────────────────
                Text(
                    text = "Identifying Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Features as ListItems with bullet icons
                val features = variant.distinguishingFeatures
                    .split(".")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                features.forEach { feature ->
                    ListItem(
                        headlineContent = { Text(feature, style = MaterialTheme.typography.bodyMedium) },
                        leadingContent = {
                            Icon(
                                Icons.Default.FiberManualRecord,
                                contentDescription = null,
                                modifier = Modifier.size(8.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }

                // ── Control methods ───────────────────────────────────────────
                Text(
                    text = "Recommended Control",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                variant.controlMethods.forEach { method ->
                    ControlMethodOutlinedCard(method = method)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ControlMethodOutlinedCard(method: TreatmentMethod) {
    val icon = treatmentMethodIcon(method)

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RangerGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = method.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
