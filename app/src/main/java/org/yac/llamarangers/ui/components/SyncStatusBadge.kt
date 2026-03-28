package org.yac.llamarangers.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.yac.llamarangers.domain.model.enums.SyncStatus
import org.yac.llamarangers.ui.theme.RangerGreen
import org.yac.llamarangers.ui.theme.RangerOrange
import org.yac.llamarangers.ui.theme.RangerRed

/**
 * Small icon badge showing sync status.
 * Ports iOS SyncStatusBadge.
 */
@Composable
fun SyncStatusBadge(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = when (status) {
        SyncStatus.SYNCED -> Icons.Default.CheckCircle to RangerGreen
        SyncStatus.PENDING_CREATE, SyncStatus.PENDING_UPDATE -> Icons.Default.CloudUpload to RangerOrange
        SyncStatus.PENDING_DELETE -> Icons.Default.Delete to RangerRed
    }

    Icon(
        imageVector = icon,
        contentDescription = "Sync: ${status.name}",
        tint = tint,
        modifier = modifier
    )
}
