package org.yac.llamarangers.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.ui.theme.RangerBlue
import org.yac.llamarangers.ui.theme.RangerOrange
import org.yac.llamarangers.ui.theme.RangerRed

/**
 * Vertical stack of layer toggle icons.
 * Ports iOS LayerToggleView.
 */
@Composable
fun LayerTogglePanel(
    showSightings: Boolean,
    showZones: Boolean,
    showPatrols: Boolean,
    onToggleSightings: () -> Unit,
    onToggleZones: () -> Unit,
    onTogglePatrols: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
    ) {
        LayerIconButton(
            icon = Icons.Default.LocationOn,
            isOn = showSightings,
            activeColor = RangerRed,
            contentDescription = "Sightings",
            onClick = onToggleSightings
        )
        HorizontalDivider(modifier = Modifier.size(width = 28.dp, height = 1.dp))
        LayerIconButton(
            icon = Icons.Default.GridView,
            isOn = showZones,
            activeColor = RangerOrange,
            contentDescription = "Zones",
            onClick = onToggleZones
        )
        HorizontalDivider(modifier = Modifier.size(width = 28.dp, height = 1.dp))
        LayerIconButton(
            icon = Icons.Default.DirectionsWalk,
            isOn = showPatrols,
            activeColor = RangerBlue,
            contentDescription = "Patrols",
            onClick = onTogglePatrols
        )
    }
}

@Composable
private fun LayerIconButton(
    icon: ImageVector,
    isOn: Boolean,
    activeColor: Color,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isOn) activeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}
