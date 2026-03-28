package org.yac.llamarangers.ui.patrol

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.data.local.entity.PatrolRecordEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Patrol history list view. Ports iOS PatrolListView.
 * Each patrol shown as an ElevatedCard with area, date, duration,
 * and a color-coded completion status chip.
 */
@Composable
fun PatrolListContent(patrols: List<PatrolRecordEntity>) {
    if (patrols.isEmpty()) {
        Text(
            "No patrols yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        patrols.forEach { patrol ->
            PatrolCard(patrol = patrol)
        }
    }
}

@Composable
private fun PatrolCard(patrol: PatrolRecordEntity) {
    val isComplete = patrol.endTime != null
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    val durationText: String = if (isComplete && patrol.endTime != null) {
        val diffMinutes = ((patrol.endTime - patrol.patrolDate) / 60_000).toInt()
        if (diffMinutes >= 60) "${diffMinutes / 60}h ${diffMinutes % 60}m"
        else "${diffMinutes}m"
    } else {
        "In progress"
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patrol.areaName ?: "Unknown Area",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateFormat.format(Date(patrol.patrolDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            PatrolStatusChip(isComplete = isComplete)
        }
    }
}

@Composable
private fun PatrolStatusChip(isComplete: Boolean) {
    val doneGreen = Color(0xFF4CAF50)
    val activeAmber = Color(0xFFFFA726)
    val (label, chipColor) = if (isComplete) "Done" to doneGreen else "Active" to activeAmber
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = chipColor
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = chipColor.copy(alpha = 0.12f)
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = chipColor.copy(alpha = 0.4f)
        )
    )
}

/**
 * Calendar view for patrol history. Ports iOS PatrolCalendarView.
 */
@Composable
fun PatrolCalendarContent(patrols: List<PatrolRecordEntity>) {
    var displayMonth by remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        mutableStateOf(cal.time)
    }

    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val weekdaySymbols = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

    Column {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val cal = Calendar.getInstance().apply { time = displayMonth }
                cal.add(Calendar.MONTH, -1)
                displayMonth = cal.time
            }) {
                Icon(Icons.Default.ChevronLeft, "Previous month")
            }
            Text(
                text = monthFormat.format(displayMonth),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = {
                val cal = Calendar.getInstance().apply { time = displayMonth }
                cal.add(Calendar.MONTH, 1)
                displayMonth = cal.time
            }) {
                Icon(Icons.Default.ChevronRight, "Next month")
            }
        }

        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdaySymbols.forEach { sym ->
                Text(
                    text = sym,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Calendar grid
        val cal = Calendar.getInstance().apply { time = displayMonth }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1

        val today = Calendar.getInstance()

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(((daysInMonth + firstDayOfWeek + 6) / 7 * 48).dp),
            userScrollEnabled = false
        ) {
            items(firstDayOfWeek) {
                Box(modifier = Modifier.height(44.dp))
            }

            items(daysInMonth) { dayIndex ->
                val day = dayIndex + 1
                val dayCal = Calendar.getInstance().apply {
                    time = displayMonth
                    set(Calendar.DAY_OF_MONTH, day)
                }
                val dayStart = dayCal.apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                }.timeInMillis
                val dayEnd = dayStart + 86_400_000L

                val dayPatrols = patrols.filter { p ->
                    val pd = p.patrolDate
                    pd in dayStart until dayEnd
                }

                val isToday = dayCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        dayCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

                Column(
                    modifier = Modifier
                        .padding(1.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (dayPatrols.isNotEmpty()) Color(0xFF4CAF50).copy(alpha = 0.07f)
                            else Color.Transparent
                        )
                        .padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isToday) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.toString(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isToday) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (dayPatrols.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.Center) {
                            dayPatrols.take(3).forEach { p ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 1.dp)
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (p.endTime != null) Color(0xFF4CAF50)
                                            else Color(0xFFFFA726)
                                        )
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
        }

        // Legend for patrols this month
        val monthCal = Calendar.getInstance().apply { time = displayMonth }
        val monthStart = monthCal.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val nextMonth = Calendar.getInstance().apply {
            time = displayMonth
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val monthPatrols = patrols.filter { it.patrolDate in monthStart until nextMonth }
            .sortedBy { it.patrolDate }

        if (monthPatrols.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Patrols this month",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val legendDateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
            monthPatrols.forEach { patrol ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (patrol.endTime != null) Color(0xFF4CAF50)
                                else Color(0xFFFFA726)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(patrol.areaName ?: "Unknown", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        legendDateFormat.format(Date(patrol.patrolDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
