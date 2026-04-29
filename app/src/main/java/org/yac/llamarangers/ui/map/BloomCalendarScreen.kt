package org.yac.llamarangers.ui.map

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.domain.model.enums.InvasiveSpecies
import java.text.SimpleDateFormat
import java.util.*

enum class RiskLevel(val label: String) {
    HIGH("HIGH RISK"),
    MODERATE("MODERATE"),
    LOW("Low");

    val color: Color
        get() = when (this) {
            HIGH -> Color(0xFFBA1A1A) // dsStatusActive
            MODERATE -> Color(0xFFC4692A) // dsAccent
            LOW -> Color(0xFFA89880) // dsInkMuted
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloomCalendarScreen(
    onNavigateBack: () -> Unit = {}
) {
    var displayMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bloom Calendar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back")
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
            // Header
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                VStack(
                    modifier = Modifier.padding(vertical = 8.dp),
                    spacing = 8.dp
                ) {
                    HStack(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = monthName(displayMonth),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    HStack(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        IconButton(onClick = {
                            displayMonth = if (displayMonth == 1) 12 else displayMonth - 1
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Text(
                            text = "Active Flowering & Seeding",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        IconButton(onClick = {
                            displayMonth = if (displayMonth == 12) 1 else displayMonth + 1
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                        }
                    }
                }
            }

            // Species list
            VStack(
                modifier = Modifier.padding(horizontal = 16.dp),
                spacing = 12.dp
            ) {
                val speciesList = InvasiveSpecies.entries.filter { it != InvasiveSpecies.UNKNOWN }
                speciesList.forEach { species ->
                    BloomSpeciesCard(species = species, month = displayMonth)
                }
            }

            // Bottom note
            HStack(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(Color(0xFFC4692A).copy(alpha = 0.08f), shape = RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .padding(16.dp),
                spacing = 12.dp
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFC4692A) // dsAccent
                )
                Text(
                    text = "Treat before seed set to prevent dispersal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BloomSpeciesCard(species: InvasiveSpecies, month: Int) {
    val (description, risk) = riskLevel(forSpecies = species, month = month)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        HStack(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(species.color)
            )

            Spacer(modifier = Modifier.width(12.dp))

            VStack(horizontalAlignment = Alignment.Start, spacing = 2.dp) {
                Text(
                    text = species.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = risk.label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .background(risk.color, shape = CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

private fun riskLevel(forSpecies: InvasiveSpecies, month: Int): Pair<String, RiskLevel> {
    return when (forSpecies) {
        InvasiveSpecies.LANTANA -> {
            if (month >= 10 || month <= 3) {
                Pair("Peak flowering — HIGH RISK", RiskLevel.HIGH)
            } else {
                Pair("Flowering year-round", RiskLevel.MODERATE)
            }
        }
        InvasiveSpecies.RUBBER_VINE -> {
            if (month in 8..10) {
                Pair("Flowers now — seeds next", RiskLevel.HIGH)
            } else if (month == 11 || month == 12 || month == 1) {
                Pair("Seeds dispersing — CRITICAL", RiskLevel.HIGH)
            } else {
                Pair("Dormant", RiskLevel.LOW)
            }
        }
        InvasiveSpecies.PRICKLY_ACACIA -> {
            if (month in 4..7) {
                Pair("Pods mature & fall — CRITICAL", RiskLevel.HIGH)
            } else {
                Pair("Off-season", RiskLevel.LOW)
            }
        }
        InvasiveSpecies.SICKLEPOD -> {
            if (month in 4..9) {
                Pair("Seeds setting — HIGH RISK", RiskLevel.HIGH)
            } else {
                Pair("Wet season growth", RiskLevel.MODERATE)
            }
        }
        InvasiveSpecies.GIANT_RATS_TAIL_GRASS -> {
            if (month in 3..6) {
                Pair("Seeds ripen — HIGH RISK", RiskLevel.HIGH)
            } else {
                Pair("Off-season", RiskLevel.LOW)
            }
        }
        InvasiveSpecies.POND_APPLE -> {
            if (month in 4..7) {
                Pair("Fruits develop — moderate risk", RiskLevel.MODERATE)
            } else {
                Pair("Dormant", RiskLevel.LOW)
            }
        }
        InvasiveSpecies.UNKNOWN -> {
            Pair("Data unavailable", RiskLevel.LOW)
        }
    }
}

private fun monthName(month: Int): String {
    val formatter = SimpleDateFormat("MMMM", Locale.getDefault())
    val cal = Calendar.getInstance().apply {
        set(Calendar.MONTH, month - 1)
    }
    return formatter.format(cal.time)
}

// Helpers for iOS-like stacks
@Composable
fun HStack(
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
fun VStack(
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

