package org.yac.llamarangers.ui.sighting

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.service.location.LocationManager

/**
 * GPS location display with accuracy indicator and re-capture button.
 * Ports iOS GPSCaptureView.
 */
@Composable
fun GPSCaptureView(
    location: Location?,
    accuracyLevel: LocationManager.AccuracyLevel,
    onRecapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Location",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (location != null) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = String.format("%.6f, %.6f", location.latitude, location.longitude),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(accuracyColor(accuracyLevel))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("\u00B1%.0fm", location.accuracy),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Acquiring GPS\u2026",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedButton(onClick = onRecapture) {
                Text("Re-capture")
            }
        }
    }
}

private fun accuracyColor(level: LocationManager.AccuracyLevel): Color {
    return when (level) {
        LocationManager.AccuracyLevel.GOOD -> Color(0xFF4CAF50)
        LocationManager.AccuracyLevel.FAIR -> Color(0xFFFFC107)
        LocationManager.AccuracyLevel.POOR -> Color(0xFFF44336)
        LocationManager.AccuracyLevel.UNKNOWN -> Color.Gray
    }
}
