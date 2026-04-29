package org.yac.llamarangers.ui.more

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.domain.model.SeasonalAlert
import org.yac.llamarangers.domain.model.enums.InfestationSize
import org.yac.llamarangers.domain.model.enums.InvasiveSpecies
import org.yac.llamarangers.ui.components.SeasonalAlertBanner
import org.yac.llamarangers.ui.components.VariantColourDot
import org.yac.llamarangers.ui.theme.RangerGreen
import org.yac.llamarangers.ui.theme.RangerOrange

/**
 * Biocontrol answer options.
 */
private enum class BiocontrolAnswer(val label: String) {
    YES("Yes"),
    NO("No"),
    UNSURE("Unsure")
}

/**
 * Control protocol decision tree screen — M3 polish pass.
 * Ports iOS ControlProtocolView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlProtocolScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    }
    val recentRain = prefs.getBoolean(SettingsViewModel.RECENT_RAIN_KEY, false)

    var selectedVariant by remember { mutableStateOf<InvasiveSpecies?>(null) }
    var selectedSize by remember { mutableStateOf<InfestationSize?>(null) }
    var biocontrolVisible by remember { mutableStateOf<BiocontrolAnswer?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control Protocol") },
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
            // Seasonal alerts
            val alerts = SeasonalAlert.activeAlerts(recentRain = recentRain)
            alerts.forEach { alert ->
                SeasonalAlertBanner(alert = alert)
            }

            // ── Step 1: Variant ───────────────────────────────────────────────
            StepCard(number = 1, question = "What variant is it?") {
                ProtocolVariantPicker(
                    selectedVariant = selectedVariant,
                    onSelect = {
                        selectedVariant = it
                        if (it != selectedVariant) biocontrolVisible = null
                    }
                )
            }

            // ── Step 2: Size ──────────────────────────────────────────────────
            if (selectedVariant != null) {
                StepCard(number = 2, question = "How large is the infestation?") {
                    ProtocolSizePicker(
                        selectedSize = selectedSize ?: InfestationSize.SMALL,
                        onSelect = { selectedSize = it }
                    )
                }
            }

            // ── Step 3: Biocontrol ────────────────────────────────────────────
            if (selectedVariant?.hasBiocontrolConcern == true) {
                StepCard(number = 3, question = "Are biocontrol insects visible?") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BiocontrolAnswer.entries.forEach { answer ->
                            val isSelected = biocontrolVisible == answer
                            Button(
                                onClick = { biocontrolVisible = answer },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) RangerOrange
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.White
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(answer.label)
                            }
                        }
                    }
                }
            }

            // ── Result card ───────────────────────────────────────────────────
            val variant = selectedVariant
            val size = selectedSize
            if (variant != null && size != null) {
                ProtocolResultCard(variant = variant, biocontrol = biocontrolVisible)
            }
        }
    }
}

@Composable
private fun StepCard(
    number: Int,
    question: String,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Step number chip
                AssistChip(
                    onClick = {},
                    label = { Text("Step $number") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(question, style = MaterialTheme.typography.titleSmall)
            }
            content()
        }
    }
}

@Composable
private fun ProtocolVariantPicker(
    selectedVariant: InvasiveSpecies?,
    onSelect: (InvasiveSpecies) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        InvasiveSpecies.entries.forEach { variant ->
            val isSelected = selectedVariant == variant
            Button(
                onClick = { onSelect(variant) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) variant.color
                    else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) {
                        val c = variant.color
                        val lum = 0.2126f * c.red + 0.7152f * c.green + 0.0722f * c.blue
                        if (lum > 0.65f) Color.Black else Color.White
                    } else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VariantColourDot(variant = variant, size = 14.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(variant.displayName)
                }
            }
        }
    }
}

@Composable
private fun ProtocolSizePicker(
    selectedSize: InfestationSize,
    onSelect: (InfestationSize) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfestationSize.entries.forEach { size ->
            val isSelected = selectedSize == size
            Button(
                onClick = { onSelect(size) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(size.displayName, style = MaterialTheme.typography.labelMedium)
                    Text(
                        size.areaDescription,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtocolResultCard(
    variant: InvasiveSpecies,
    biocontrol: BiocontrolAnswer?
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = RangerGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Recommendation",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (biocontrol == BiocontrolAnswer.YES) {
                Text(
                    "Biocontrol insects detected \u2014 do NOT spray. Allow insects to feed. Monitor in 3\u20134 weeks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RangerOrange
                )
            } else {
                variant.controlMethods.forEach { method ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                method.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                method.instructions,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
