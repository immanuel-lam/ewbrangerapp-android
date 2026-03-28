package org.yac.llamarangers.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.domain.model.SeasonalAlert
import org.yac.llamarangers.ui.theme.RangerOrangeContainer
import org.yac.llamarangers.ui.theme.RangerOrange

/**
 * Alert banner for seasonal guidance.
 * WARNING uses amber/orange container (warm seasonal tone).
 * INFO uses tertiaryContainer; CRITICAL uses errorContainer.
 * Ports iOS SeasonalAlertBanner.
 */
@Composable
fun SeasonalAlertBanner(
    alert: SeasonalAlert,
    modifier: Modifier = Modifier
) {
    data class BannerStyle(
        val icon: ImageVector,
        val iconColor: Color,
        val containerColor: Color,
        val contentColor: Color
    )

    @Composable
    fun style(): BannerStyle = when (alert.severity) {
        SeasonalAlert.Severity.INFO -> BannerStyle(
            icon = Icons.Default.Info,
            iconColor = MaterialTheme.colorScheme.tertiary,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        SeasonalAlert.Severity.WARNING -> BannerStyle(
            icon = Icons.Default.Warning,
            iconColor = RangerOrange,
            containerColor = RangerOrangeContainer,
            contentColor = RangerOrange
        )
        SeasonalAlert.Severity.CRITICAL -> BannerStyle(
            icon = Icons.Default.Error,
            iconColor = MaterialTheme.colorScheme.error,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    }

    val s = style()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // 12dp corner radius
        color = s.containerColor,
        contentColor = s.contentColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = s.icon,
                contentDescription = alert.severity.name,
                tint = s.iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = s.contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}
