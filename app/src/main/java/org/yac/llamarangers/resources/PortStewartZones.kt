package org.yac.llamarangers.resources

import org.yac.llamarangers.domain.model.PatrolChecklistItem
import java.util.*

/**
 * Static geographic data for Port Stewart, Cape York.
 * Ports iOS PortStewartZones.
 */
object PortStewartZones {

    data class LatLng(val latitude: Double, val longitude: Double)

    val areaCoordinates: Map<String, LatLng> = mapOf(
        "North Beach Dunes" to LatLng(-14.677, 143.702),
        "River Mouth Flats" to LatLng(-14.711, 143.722),
        "Camping Ground Perimeter" to LatLng(-14.700, 143.699),
        "Airstrip Corridor" to LatLng(-14.720, 143.690),
        "Southern Scrub Belt" to LatLng(-14.740, 143.703),
        "Creek Line East" to LatLng(-14.708, 143.730),
        "Creek Line West" to LatLng(-14.708, 143.678),
        "Headland Track" to LatLng(-14.688, 143.718),
        "Mangrove Edge" to LatLng(-14.728, 143.712),
        "Central Clearing" to LatLng(-14.710, 143.700)
    )

    val patrolAreas = listOf(
        "North Beach Dunes",
        "River Mouth Flats",
        "Camping Ground Perimeter",
        "Airstrip Corridor",
        "Southern Scrub Belt",
        "Creek Line East",
        "Creek Line West",
        "Headland Track",
        "Mangrove Edge",
        "Central Clearing"
    )

    val defaultChecklistBase = listOf(
        PatrolChecklistItem(label = "Check GPS is recording", timeEstimateMins = 2),
        PatrolChecklistItem(label = "Photograph new infestations", timeEstimateMins = 15),
        PatrolChecklistItem(label = "Record all invasive plant sightings", timeEstimateMins = 20),
        PatrolChecklistItem(label = "Check previous treatment sites", timeEstimateMins = 15),
        PatrolChecklistItem(label = "Note regrowth on treated plants", timeEstimateMins = 10),
        PatrolChecklistItem(label = "Check herbicide supply before departing", timeEstimateMins = 5)
    )

    fun defaultChecklist(forArea: String): List<PatrolChecklistItem> {
        val items = mutableListOf<PatrolChecklistItem>()
        items.add(PatrolChecklistItem(label = "Walk full boundary of $forArea", timeEstimateMins = 20))
        items.addAll(defaultChecklistBase)
        
        // Ensure unique IDs (demo: simple UUIDs)
        return items.map { it.copy(id = UUID.randomUUID().toString()) }
    }
}
