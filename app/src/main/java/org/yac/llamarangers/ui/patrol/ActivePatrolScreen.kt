package org.yac.llamarangers.ui.patrol

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.domain.model.PatrolChecklistItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Active patrol view with checklist and progress.
 * Ports iOS ActivePatrolView.
 * LinearProgressIndicator for checklist progress; checklist items as ListItem
 * with Checkbox leadingContent; "Finish Patrol" FilledButton with errorContainer colors.
 */
@Composable
fun ActivePatrolScreen(
    viewModel: PatrolViewModel
) {
    val activePatrol by viewModel.activePatrol.collectAsState()
    val checklistItems by viewModel.activeChecklistItems.collectAsState()
    val completionPct = viewModel.completionPercentage

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with progress
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = activePatrol?.areaName ?: "Active Patrol",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "${(completionPct * 100).toInt()}% complete",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            LinearProgressIndicator(
                progress = { completionPct },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        // Checklist
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(checklistItems, key = { it.id }) { item ->
                ChecklistListItem(
                    item = item,
                    onToggle = { viewModel.toggleItem(item) }
                )
            }
        }

        // Finish Patrol button — FilledButton with errorContainer colors
        Button(
            onClick = { viewModel.finishPatrol() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text(
                text = "Finish Patrol",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun ChecklistListItem(
    item: PatrolChecklistItem,
    onToggle: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    ListItem(
        headlineContent = {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isComplete) TextDecoration.LineThrough else null,
                color = if (item.isComplete) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = item.completedAt?.let { millis ->
            {
                Text(
                    text = timeFormat.format(Date(millis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Checkbox(
                checked = item.isComplete,
                onCheckedChange = { onToggle() }
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
