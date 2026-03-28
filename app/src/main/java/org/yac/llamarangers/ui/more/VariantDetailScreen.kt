package org.yac.llamarangers.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.domain.model.SeasonalAlert
import org.yac.llamarangers.domain.model.enums.LantanaVariant
import org.yac.llamarangers.domain.model.enums.TreatmentMethod
import org.yac.llamarangers.ui.components.SeasonalAlertBanner
import org.yac.llamarangers.ui.theme.RangerGreen

/**
 * Variant detail screen showing identifying features and control methods.
 * Ports iOS VariantDetailView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariantDetailScreen(
    variantValue: String,
    onNavigateBack: () -> Unit = {}
) {
    val variant = LantanaVariant.fromValue(variantValue)
    val headerTextColor = if (variant.color.luminance() > 0.65f) Color.Black else Color.White

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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Colour swatch header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(variant.color, variant.color.copy(alpha = 0.7f))
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = variant.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = headerTextColor
                    )
                    Text(
                        text = "Lantana camara var.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = headerTextColor.copy(alpha = 0.7f)
                    )
                }
            }

            // Biocontrol warning banner (pink variant)
            if (variant.hasBiocontrolConcern) {
                SeasonalAlertBanner(
                    alert = SeasonalAlert(
                        title = "Check for Biocontrol Insects",
                        message = "During the wet season (Nov\u2013Mar), check for lantana bug before applying chemicals to pink Lantana.",
                        severity = SeasonalAlert.Severity.WARNING
                    )
                )
            }

            // Identifying features
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Identifying Features", style = MaterialTheme.typography.titleMedium)
                Text(
                    variant.distinguishingFeatures,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Recommended control methods
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Recommended Control", style = MaterialTheme.typography.titleMedium)
                variant.controlMethods.forEach { method ->
                    ControlMethodCard(method = method)
                }
            }
        }
    }
}

@Composable
private fun ControlMethodCard(method: TreatmentMethod) {
    val icon = treatmentMethodIcon(method)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
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
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = method.instructions,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun treatmentMethodIcon(method: TreatmentMethod): ImageVector = when (method) {
    TreatmentMethod.CUT_STUMP -> Icons.Default.ContentCut
    TreatmentMethod.SPLAT_GUN -> Icons.Default.CropSquare
    TreatmentMethod.FOLIAR_SPRAY -> Icons.Default.WaterDrop
    TreatmentMethod.BASAL_BARK -> Icons.Default.Park
}
