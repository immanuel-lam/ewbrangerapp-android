package org.yac.llamarangers.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.yac.llamarangers.ui.activity.ActivityScreen
import org.yac.llamarangers.ui.guide.SpeciesGuideScreen
import org.yac.llamarangers.ui.hub.HubScreen
import org.yac.llamarangers.ui.map.MapScreen
import org.yac.llamarangers.ui.navigation.Screen
import org.yac.llamarangers.ui.safety.SafetyCheckInScreen
import org.yac.llamarangers.ui.theme.AppThemeViewModel
import org.yac.llamarangers.ui.more.DashboardViewModel

/**
 * Bottom navigation with 5 tabs: Map, Activity, Guide, Safety, Hub.
 * Ports iOS v3 MainTabView.
 */

private data class TabItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val tabs = listOf(
    TabItem(Screen.Map, "Map", Icons.Default.Map),
    TabItem(Screen.Activity, "Activity", Icons.AutoMirrored.Filled.List),
    TabItem(Screen.SpeciesGuide, "Guide", Icons.Default.Spa),
    TabItem(Screen.Safety, "Safety", Icons.Default.Shield),
    TabItem(Screen.Hub, "Hub", Icons.Default.GridView)
)

@Composable
fun MainTabScreen(
    onNavigateToLogSighting: () -> Unit = {},
    onNavigateToSightingDetail: (String) -> Unit = {},
    onNavigateToAddZone: () -> Unit = {},
    onNavigateToZoneDetail: (String) -> Unit = {},
    onNavigateToSpeciesDetail: (String) -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToPesticideList: () -> Unit = {},
    onNavigateToMeshSync: () -> Unit = {},
    onNavigateToCloudSync: () -> Unit = {},
    onNavigateToShiftHandover: () -> Unit = {},
    onNavigateToZoneList: () -> Unit = {},
    onNavigateToEquipment: () -> Unit = {},
    onNavigateToHazards: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAddTask: () -> Unit = {},
    onLogout: () -> Unit = {},
    themeViewModel: AppThemeViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val ranger by dashboardViewModel.currentRanger.collectAsState()
    val isRedLightMode by themeViewModel.isRedLightMode.collectAsState()

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
        Box(modifier = Modifier.fillMaxSize()) {
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
                composable(Screen.Activity.route) {
                    ActivityScreen(
                        onNavigateToLogSighting = onNavigateToLogSighting,
                        onNavigateToSightingDetail = onNavigateToSightingDetail,
                        onNavigateToAddTask = onNavigateToAddTask
                    )
                }
                composable(Screen.SpeciesGuide.route) {
                    SpeciesGuideScreen(
                        onNavigateToDetail = onNavigateToSpeciesDetail
                    )
                }
                composable(Screen.Safety.route) {
                    SafetyCheckInScreen()
                }
                composable(Screen.Hub.route) {
                    HubScreen(
                        ranger = ranger,
                        onLogout = onLogout,
                        onNavigateToDashboard = onNavigateToDashboard,
                        onNavigateToSupplies = onNavigateToPesticideList,
                        onNavigateToDaySync = onNavigateToMeshSync,
                        onNavigateToZones = onNavigateToZoneList,
                        onNavigateToCloudSync = onNavigateToCloudSync,
                        onNavigateToHandover = onNavigateToShiftHandover,
                        onNavigateToEquipment = onNavigateToEquipment,
                        onNavigateToHazards = onNavigateToHazards,
                        onNavigateToSettings = onNavigateToSettings
                    )
                }
            }

            // Red Light Mode Overlay
            if (isRedLightMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red.copy(alpha = 0.4f))
                        .pointerInput(Unit) { /* ignore touches */ },
                    contentAlignment = Alignment.Center
                ) {}
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Dummy() {} // just for experimental import if needed
