package org.yac.llamarangers.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.yac.llamarangers.data.repository.ZoneRepository
import org.yac.llamarangers.service.auth.AuthManager
import org.yac.llamarangers.ui.app.MainTabScreen
import org.yac.llamarangers.ui.login.LoginScreen
import org.yac.llamarangers.ui.map.AddZoneScreen
import org.yac.llamarangers.ui.map.ZoneDetailScreen
import org.yac.llamarangers.ui.map.ZoneListScreen
import org.yac.llamarangers.ui.more.ControlProtocolScreen
import org.yac.llamarangers.ui.more.DashboardScreen
import org.yac.llamarangers.ui.more.MeshSyncScreen
import org.yac.llamarangers.ui.more.PesticideDetailScreen
import org.yac.llamarangers.ui.more.PesticideListScreen
import org.yac.llamarangers.ui.more.SettingsScreen
import org.yac.llamarangers.ui.more.VariantDetailScreen
import org.yac.llamarangers.ui.more.VariantGuideScreen
import org.yac.llamarangers.ui.sighting.LogSightingScreen
import org.yac.llamarangers.ui.sighting.SightingDetailScreen
import org.yac.llamarangers.ui.sighting.TreatmentEntryScreen
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
                onNavigateToVariantGuide = { navController.navigate(Screen.VariantGuide.route) },
                onNavigateToControlProtocol = { navController.navigate(Screen.ControlProtocol.route) },
                onNavigateToDashboard = { navController.navigate(Screen.Dashboard.route) },
                onNavigateToPesticideList = { navController.navigate(Screen.PesticideList.route) },
                onNavigateToMeshSync = { navController.navigate(Screen.MeshSync.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToTreatmentEntry = { id -> navController.navigate(Screen.TreatmentEntry(id).route) },
                onNavigateToAddTask = { navController.navigate(Screen.AddTask.route) }
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
                onNavigateToTreatmentEntry = { id -> navController.navigate(Screen.TreatmentEntry(id).route) }
            )
        }

        composable(
            route = Screen.TreatmentEntry.ROUTE,
            arguments = listOf(navArgument(Screen.TreatmentEntry.ARG_SIGHTING_ID) { type = NavType.StringType })
        ) {
            TreatmentEntryScreen(onNavigateBack = { navController.popBackStack() })
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

        // More section
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
            MeshSyncScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.VariantGuide.route) {
            VariantGuideScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { v -> navController.navigate(Screen.VariantDetail(v).route) }
            )
        }

        composable(
            route = Screen.VariantDetail.ROUTE,
            arguments = listOf(navArgument(Screen.VariantDetail.ARG_VARIANT_VALUE) { type = NavType.StringType })
        ) { backStackEntry ->
            val variantValue = backStackEntry.arguments?.getString(Screen.VariantDetail.ARG_VARIANT_VALUE) ?: ""
            VariantDetailScreen(
                variantValue = variantValue,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ControlProtocol.route) {
            ControlProtocolScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
