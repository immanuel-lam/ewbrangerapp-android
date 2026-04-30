package org.yac.llamarangers.ui.activity

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.ui.patrol.PatrolScreen
import org.yac.llamarangers.ui.sighting.SightingListScreen
import org.yac.llamarangers.ui.tasks.TaskListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    onNavigateToLogSighting: () -> Unit = {},
    onNavigateToSightingDetail: (String) -> Unit = {},
    onNavigateToAddTask: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Sightings", "Patrols", "Tasks")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider() }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> SightingListScreen(
                        onNavigateToLogSighting = onNavigateToLogSighting,
                        onNavigateToSightingDetail = onNavigateToSightingDetail
                    )
                    1 -> PatrolScreen()
                    2 -> TaskListScreen(
                        onNavigateToAddTask = onNavigateToAddTask
                    )
                }
            }
        }
    }
}
