package org.yac.llamarangers.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.yac.llamarangers.data.repository.ZoneRepository
import org.yac.llamarangers.service.auth.AuthManager
import org.yac.llamarangers.ui.app.MainTabScreen
import org.yac.llamarangers.ui.equipment.EquipmentListScreen
import org.yac.llamarangers.ui.guide.SpeciesDetailScreen
import org.yac.llamarangers.ui.guide.SpeciesGuideScreen
import org.yac.llamarangers.ui.hazard.HazardLogScreen
import org.yac.llamarangers.ui.hazard.LogHazardScreen
import org.yac.llamarangers.ui.hub.ConflictResolverScreen
import org.yac.llamarangers.ui.hub.DemoLiveSyncScreen
import org.yac.llamarangers.ui.hub.ShiftHandoverScreen
import org.yac.llamarangers.ui.login.LoginScreen
import org.yac.llamarangers.ui.map.AddZoneScreen
import org.yac.llamarangers.ui.map.BloomCalendarScreen
import org.yac.llamarangers.ui.map.ZoneDetailScreen
import org.yac.llamarangers.ui.map.ZoneListScreen
import org.yac.llamarangers.ui.more.ControlProtocolScreen
import org.yac.llamarangers.ui.more.DashboardScreen
import org.yac.llamarangers.ui.more.MeshSyncScreen
import org.yac.llamarangers.ui.more.PesticideDetailScreen
import org.yac.llamarangers.ui.more.PesticideListScreen
import org.yac.llamarangers.ui.more.SettingsScreen
import org.yac.llamarangers.ui.sighting.LogSightingScreen
import org.yac.llamarangers.ui.sighting.SightingDetailScreen
import org.yac.llamarangers.ui.sighting.TreatmentEntryScreen
import org.yac.llamarangers.ui.sighting.TreatmentFollowUpScreen
import org.yac.llamarangers.ui.tasks.AddTaskScreen

@Composable
fun AppNavigation(
    authManager: AuthManager,
    zoneRepository: ZoneRepository
) {
    val isAuthenticated by authManager.isAuthenticated.collectAsState()
    val navController = rememberNavController()

    val startDestination = if (isAuthenticated) Screen.MainTabs.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth
        composable(Screen.Login.route) {
            LoginScreen()
        }

        // Main tabs container
        composable(Screen.MainTabs.route) {
            MainTabScreen(
                onNavigateToLogSighting = { navController.navigate(Screen.LogSighting.route) },
                onNavigateToSightingDetail = { id -> navController.navigate(Screen.SightingDetail(id).route) },
                onNavigateToAddZone = { navController.navigate(Screen.AddZone.route) },
                onNavigateToZoneList = { navController.navigate(Screen.ZoneList.route) },
                onNavigateToZoneDetail = { id -> navController.navigate(Screen.ZoneDetail(id).route) },
                onNavigateToSpeciesDetail = { v -> navController.navigate(Screen.SpeciesDetail(v).route) },
                onNavigateToDashboard = { navController.navigate(Screen.Dashboard.route) },
                onNavigateToPesticideList = { navController.navigate(Screen.PesticideList.route) },
                onNavigateToMeshSync = { navController.navigate(Screen.MeshSync.route) },
                onNavigateToCloudSync = { navController.navigate(Screen.CloudSync.route) },
                onNavigateToShiftHandover = { navController.navigate(Screen.ShiftHandover.route) },
                onNavigateToEquipment = { navController.navigate(Screen.EquipmentList.route) },
                onNavigateToHazards = { navController.navigate(Screen.HazardLog.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAddTask = { navController.navigate(Screen.AddTask.route) },
                onLogout = { authManager.logout() }
            )
        }

        // Sighting flow
        composable(Screen.LogSighting.route) {
            LogSightingScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.SightingDetail.ROUTE,
            arguments = listOf(navArgument(Screen.SightingDetail.ARG_SIGHTING_ID) { type = NavType.StringType })
        ) {
            SightingDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTreatmentEntry = { id -> navController.navigate(Screen.TreatmentEntry(id).route) },
                onNavigateToTreatmentFollowUp = { id -> navController.navigate(Screen.TreatmentFollowUp(id).route) }
            )
        }

        composable(
            route = Screen.TreatmentEntry.ROUTE,
            arguments = listOf(navArgument(Screen.TreatmentEntry.ARG_SIGHTING_ID) { type = NavType.StringType })
        ) {
            TreatmentEntryScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.TreatmentFollowUp.ROUTE,
            arguments = listOf(navArgument(Screen.TreatmentFollowUp.ARG_TREATMENT_ID) { type = NavType.StringType })
        ) {
            TreatmentFollowUpScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Task flow
        composable(Screen.AddTask.route) {
            AddTaskScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Zone flow
        composable(Screen.ZoneList.route) {
            ZoneListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToZoneDetail = { id -> navController.navigate(Screen.ZoneDetail(id).route) },
                onNavigateToAddZone = { navController.navigate(Screen.AddZone.route) }
            )
        }

        composable(
            route = Screen.ZoneDetail.ROUTE,
            arguments = listOf(navArgument(Screen.ZoneDetail.ARG_ZONE_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val zoneId = backStackEntry.arguments?.getString(Screen.ZoneDetail.ARG_ZONE_ID) ?: ""
            ZoneDetailScreen(
                zoneId = zoneId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddZone.route) {
            AddZoneScreen(
                zoneRepository = zoneRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Hub Destinations
        composable(Screen.Dashboard.route) {
            DashboardScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.PesticideList.route) {
            PesticideListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate(Screen.PesticideDetail(id).route) }
            )
        }

        composable(
            route = Screen.PesticideDetail.ROUTE,
            arguments = listOf(navArgument(Screen.PesticideDetail.ARG_PESTICIDE_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val pesticideId = backStackEntry.arguments?.getString(Screen.PesticideDetail.ARG_PESTICIDE_ID) ?: ""
            val currentRangerId by authManager.currentRangerId.collectAsState()
            PesticideDetailScreen(
                pesticideId = pesticideId,
                rangerId = currentRangerId?.toString() ?: "",
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogUsage = { navController.popBackStack() }
            )
        }

        composable(Screen.MeshSync.route) {
            MeshSyncScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRangerStatus = { navController.navigate(Screen.RangerStatus.route) }
            )
        }

        composable(Screen.CloudSync.route) {
            DemoLiveSyncScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.ShiftHandover.route) {
            ShiftHandoverScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.ConflictResolver.route) {
            ConflictResolverScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.RangerStatus.route) {
            org.yac.llamarangers.ui.mesh.RangerStatusScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.BloomCalendar.route) {
            BloomCalendarScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.SpeciesDetail.ROUTE,
            arguments = listOf(navArgument(Screen.SpeciesDetail.ARG_SPECIES_VALUE) { type = NavType.StringType })
        ) { backStackEntry ->
            val speciesValue = backStackEntry.arguments?.getString(Screen.SpeciesDetail.ARG_SPECIES_VALUE) ?: ""
            SpeciesDetailScreen(
                speciesValue = speciesValue,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ControlProtocol.route) {
            ControlProtocolScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.EquipmentList.route) {
            EquipmentListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddEquipment = { navController.navigate(Screen.AddEquipment.route) }
            )
        }

        composable(Screen.AddEquipment.route) {
            org.yac.llamarangers.ui.equipment.AddEquipmentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.HazardLog.route) {
            HazardLogScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogHazard = { navController.navigate(Screen.LogHazard.route) }
            )
        }
        
        composable(Screen.LogHazard.route) {
            LogHazardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(title: String, onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Coming Soon",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
