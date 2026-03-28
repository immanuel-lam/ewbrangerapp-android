package org.yac.llamarangers.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
 * Floating card displayed over the map when a marker or zone is tapped.
 * Centered on screen with a material surface.
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
        Surface(
            modifier = Modifier
                .widthIn(max = 240.dp)
                .clickable(enabled = false, onClick = {}),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Column {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                    data.subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                HorizontalDivider()

                // Actions
                data.actions.forEachIndexed { index, action ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                action.handler()
                                onDismiss()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        action.icon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (action.isDestructive) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = action.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (action.isDestructive) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (index < data.actions.lastIndex) {
                        HorizontalDivider()
                    }
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
