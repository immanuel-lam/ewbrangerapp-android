package org.yac.llamarangers.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.domain.model.enums.SyncStatus
import org.yac.llamarangers.ui.theme.RangerGreen
import org.yac.llamarangers.ui.theme.RangerRed
import org.yac.llamarangers.ui.theme.RangerYellow

/**
 * 8dp filled circle indicating sync status.
 * Green = synced, yellow = pending, red = error/pending-delete, grey = unknown.
 * No text — purely colour-coded for compact list rows.
 */
@Composable
fun SyncStatusBadge(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    val dotColor: Color = when (status) {
        SyncStatus.SYNCED -> RangerGreen
        SyncStatus.PENDING_CREATE, SyncStatus.PENDING_UPDATE -> RangerYellow
        SyncStatus.PENDING_DELETE -> RangerRed
    }

    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(dotColor)
    )
}
