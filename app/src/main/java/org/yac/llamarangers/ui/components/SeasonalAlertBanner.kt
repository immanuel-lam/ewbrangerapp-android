package org.yac.llamarangers.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.domain.model.SeasonalAlert

/**
 * Alert banner showing seasonal guidance.
 * Uses M3 color scheme containers for semantic severity rather than raw hex tints.
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
        val bgColor: Color,
        val textColor: Color
    )

    @Composable
    fun style(): BannerStyle = when (alert.severity) {
        SeasonalAlert.Severity.INFO -> BannerStyle(
            icon = Icons.Default.Info,
            iconColor = MaterialTheme.colorScheme.tertiary,
            bgColor = MaterialTheme.colorScheme.tertiaryContainer,
            textColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        SeasonalAlert.Severity.WARNING -> BannerStyle(
            icon = Icons.Default.Warning,
            iconColor = MaterialTheme.colorScheme.secondary,
            bgColor = MaterialTheme.colorScheme.secondaryContainer,
            textColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        SeasonalAlert.Severity.CRITICAL -> BannerStyle(
            icon = Icons.Default.Error,
            iconColor = MaterialTheme.colorScheme.error,
            bgColor = MaterialTheme.colorScheme.errorContainer,
            textColor = MaterialTheme.colorScheme.onErrorContainer
        )
    }

    val s = style()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(s.bgColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
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
                style = MaterialTheme.typography.labelLarge,
                color = s.textColor
            )
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodySmall,
                color = s.textColor.copy(alpha = 0.8f)
            )
        }
    }
}
