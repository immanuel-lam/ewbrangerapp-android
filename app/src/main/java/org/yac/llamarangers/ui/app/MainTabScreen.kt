package org.yac.llamarangers.ui.app

import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.*
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.yac.llamarangers.ui.map.MapScreen
import org.yac.llamarangers.ui.navigation.Screen
import org.yac.llamarangers.ui.patrol.PatrolScreen
import org.yac.llamarangers.ui.sighting.SightingListScreen
import org.yac.llamarangers.ui.tasks.TaskListScreen

/**
 * Bottom navigation with 5 tabs: Map, Sightings, Patrol, Tasks, More.
 * Ports iOS MainTabView.
 */

private data class TabItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val tabs = listOf(
    TabItem(Screen.Map, "Map", Icons.Default.Map),
    TabItem(Screen.SightingList, "Log", Icons.AutoMirrored.Filled.List),
    TabItem(Screen.Patrol, "Patrol", Icons.AutoMirrored.Filled.DirectionsWalk),
    TabItem(Screen.TaskList, "Tasks", Icons.Default.Checklist),
    TabItem(Screen.More, "More", Icons.Default.MoreHoriz)
)

@Composable
fun MainTabScreen(
    onNavigateToLogSighting: () -> Unit = {},
    onNavigateToSightingDetail: (String) -> Unit = {},
    onNavigateToAddZone: () -> Unit = {},
    onNavigateToZoneList: () -> Unit = {},
    onNavigateToZoneDetail: (String) -> Unit = {},
    onNavigateToVariantGuide: () -> Unit = {},
    onNavigateToControlProtocol: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToPesticideList: () -> Unit = {},
    onNavigateToMeshSync: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToTreatmentEntry: (String) -> Unit = {},
    onNavigateToAddTask: () -> Unit = {}
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                tabs.forEach { tab ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == tab.screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            navController.navigate(tab.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Map.route,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(Screen.Map.route) {
                MapScreen(
                    onNavigateToLogSighting = onNavigateToLogSighting,
                    onNavigateToSightingDetail = onNavigateToSightingDetail,
                    onNavigateToAddZone = onNavigateToAddZone,
                    onNavigateToZoneDetail = onNavigateToZoneDetail
                )
            }
            composable(Screen.SightingList.route) {
                SightingListScreen(
                    onNavigateToLogSighting = onNavigateToLogSighting,
                    onNavigateToSightingDetail = onNavigateToSightingDetail
                )
            }
            composable(Screen.Patrol.route) {
                PatrolScreen()
            }
            composable(Screen.TaskList.route) {
                TaskListScreen(
                    onNavigateToAddTask = onNavigateToAddTask
                )
            }
            composable(Screen.More.route) {
                MoreScreen(
                    onNavigateToVariantGuide = onNavigateToVariantGuide,
                    onNavigateToControlProtocol = onNavigateToControlProtocol,
                    onNavigateToZoneList = onNavigateToZoneList,
                    onNavigateToDashboard = onNavigateToDashboard,
                    onNavigateToPesticideList = onNavigateToPesticideList,
                    onNavigateToMeshSync = onNavigateToMeshSync,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title \u2014 Coming Soon",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
