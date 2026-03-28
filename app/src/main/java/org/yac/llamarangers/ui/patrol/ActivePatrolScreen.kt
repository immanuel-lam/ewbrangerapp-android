package org.yac.llamarangers.ui.patrol

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.domain.model.PatrolChecklistItem
import org.yac.llamarangers.ui.components.LargeButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Active patrol view with checklist and progress.
 * Ports iOS ActivePatrolView.
 */
@Composable
fun ActivePatrolScreen(
    viewModel: PatrolViewModel
) {
    val activePatrol by viewModel.activePatrol.collectAsState()
    val checklistItems by viewModel.activeChecklistItems.collectAsState()
    val completionPct = viewModel.completionPercentage

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = activePatrol?.areaName ?: "Active Patrol",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { completionPct },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(completionPct * 100).toInt()}% complete",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Checklist
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(checklistItems, key = { it.id }) { item ->
                ChecklistItemRow(
                    item = item,
                    onToggle = { viewModel.toggleItem(item) }
                )
            }
        }

        // Finish button
        LargeButton(
            title = "Finish Patrol",
            onClick = { viewModel.finishPatrol() },
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ChecklistItemRow(
    item: PatrolChecklistItem,
    onToggle: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (item.isComplete) Icons.Default.CheckBox
            else Icons.Default.CheckBoxOutlineBlank,
            contentDescription = if (item.isComplete) "Complete" else "Incomplete",
            tint = if (item.isComplete) Color(0xFF4CAF50)
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isComplete) TextDecoration.LineThrough else null,
                color = if (item.isComplete) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
            item.completedAt?.let { millis ->
                Text(
                    text = timeFormat.format(Date(millis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
