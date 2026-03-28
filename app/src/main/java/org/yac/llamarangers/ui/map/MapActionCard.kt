package org.yac.llamarangers.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Data for a floating action card anchored near a map marker or zone overlay.
 * Ports iOS MapActionCardData / MapActionCard.
 */
data class MapActionCardData(
    val title: String,
    val subtitle: String? = null,
    val sightingId: String? = null,
    val zoneId: String? = null,
    val actions: List<MapCardAction> = emptyList()
)

data class MapCardAction(
    val label: String,
    val isDestructive: Boolean = false,
    val icon: ImageVector? = null,
    val handler: () -> Unit
)

/**
 * Floating ElevatedCard displayed over the map when a marker or zone is tapped.
 * Centered on screen. Title in titleMedium; action rows use ListItem with leadingIcon.
 */
@Composable
fun MapActionCard(
    data: MapActionCardData,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clickable(enabled = false, onClick = {}),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
        ) {
            // Header
            ListItem(
                headlineContent = {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                },
                supportingContent = data.subtitle?.let { sub ->
                    {
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )

            if (data.actions.isNotEmpty()) {
                HorizontalDivider()
            }

            // Actions
            data.actions.forEachIndexed { index, action ->
                val iconColor = if (action.isDestructive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
                val textColor = if (action.isDestructive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface

                ListItem(
                    headlineContent = {
                        Text(
                            text = action.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    },
                    leadingContent = action.icon?.let { icon ->
                        {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconColor
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.clickable {
                        action.handler()
                        onDismiss()
                    }
                )
                if (index < data.actions.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

/** Helper to pick an icon for common action labels. */
fun iconForAction(label: String): ImageVector? = when {
    label.contains("Detail", ignoreCase = true) -> Icons.Default.Info
    label.contains("Edit", ignoreCase = true) -> Icons.Default.Edit
    label.contains("Delete", ignoreCase = true) -> Icons.Default.Delete
    else -> null
}
