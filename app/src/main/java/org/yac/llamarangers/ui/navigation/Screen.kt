package org.yac.llamarangers.ui.navigation

/**
 * Sealed class defining all navigation routes in the app.
 * Ports iOS NavigationStack/NavigationLink structure.
 */
sealed class Screen(val route: String) {
    // Auth
    data object Login : Screen("login")

    // Main tabs container
    data object MainTabs : Screen("main_tabs")

    // Tab destinations (displayed inside MainTabs bottom nav)
    data object Map : Screen("map")
    data object SightingList : Screen("sighting_list")
    data object Patrol : Screen("patrol")
    data object TaskList : Screen("task_list")
    data object More : Screen("more")

    // Sighting flow
    data object LogSighting : Screen("log_sighting")
    data class SightingDetail(val sightingId: String) : Screen("sighting_detail/$sightingId") {
        companion object {
            const val ROUTE = "sighting_detail/{sightingId}"
            const val ARG_SIGHTING_ID = "sightingId"
        }
    }

    // Treatment
    data class TreatmentEntry(val sightingId: String) : Screen("treatment_entry/$sightingId") {
        companion object {
            const val ROUTE = "treatment_entry/{sightingId}"
            const val ARG_SIGHTING_ID = "sightingId"
        }
    }

    // Zone flow
    data object ZoneList : Screen("zone_list")
    data class ZoneDetail(val zoneId: String) : Screen("zone_detail/$zoneId") {
        companion object {
            const val ROUTE = "zone_detail/{zoneId}"
            const val ARG_ZONE_ID = "zoneId"
        }
    }
    data object AddZone : Screen("add_zone")

    // More menu destinations
    data object Dashboard : Screen("dashboard")
    data object PesticideList : Screen("pesticide_list")
    data class PesticideDetail(val pesticideId: String) : Screen("pesticide_detail/$pesticideId") {
        companion object {
            const val ROUTE = "pesticide_detail/{pesticideId}"
            const val ARG_PESTICIDE_ID = "pesticideId"
        }
    }
    data object LogUsage : Screen("log_usage")
    data object Settings : Screen("settings")
    data object MeshSync : Screen("mesh_sync")
    data object VariantGuide : Screen("variant_guide")
    data class VariantDetail(val variantValue: String) : Screen("variant_detail/$variantValue") {
        companion object {
            const val ROUTE = "variant_detail/{variantValue}"
            const val ARG_VARIANT_VALUE = "variantValue"
        }
    }
    data object ControlProtocol : Screen("control_protocol")

    // Patrol
    data object AddTask : Screen("add_task")
}
