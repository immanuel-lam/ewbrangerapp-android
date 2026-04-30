package org.yac.llamarangers.ui.navigation

/**
 * Sealed class defining all navigation routes in the app.
 * Ports iOS v3 Navigation structure.
 */
sealed class Screen(val route: String) {
    // Auth
    data object Login : Screen("login")

    // Main tabs container
    data object MainTabs : Screen("main_tabs")

    // Tab destinations (displayed inside MainTabs bottom nav)
    data object Map : Screen("map")
    data object Activity : Screen("activity")
    data object SpeciesGuide : Screen("species_guide")
    data object Safety : Screen("safety")
    data object Hub : Screen("hub")

    // Sighting flow
    data object LogSighting : Screen("log_sighting")
    data class SightingDetail(val sightingId: String) : Screen("sighting_detail/$sightingId") {
        companion object {
            const val ROUTE = "sighting_detail/{sightingId}"
            const val ARG_SIGHTING_ID = "sightingId"
        }
    }

    data class TreatmentFollowUp(val treatmentId: String) : Screen("treatment_follow_up/$treatmentId") {
        companion object {
            const val ROUTE = "treatment_follow_up/{treatmentId}"
            const val ARG_TREATMENT_ID = "treatmentId"
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

    // Hub destinations
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
    data object CloudSync : Screen("cloud_sync")
    data object ShiftHandover : Screen("shift_handover")
    data object ConflictResolver : Screen("conflict_resolver")
    data object RangerStatus : Screen("ranger_status")
    data object EquipmentList : Screen("equipment_list")
    data object AddEquipment : Screen("add_equipment")
    data object HazardLog : Screen("hazard_log")
    data object BloomCalendar : Screen("bloom_calendar")

    // Species Guide
    data class SpeciesDetail(val speciesValue: String) : Screen("species_detail/$speciesValue") {
        companion object {
            const val ROUTE = "species_detail/{speciesValue}"
            const val ARG_SPECIES_VALUE = "speciesValue"
        }
    }
    data object ControlProtocol : Screen("control_protocol")

    // Tasks
    data object AddTask : Screen("add_task")
    
    // Hazards
    data object LogHazard : Screen("log_hazard")
}
