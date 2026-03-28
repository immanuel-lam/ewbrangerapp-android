package org.yac.llamarangers.resources

import org.yac.llamarangers.domain.model.PatrolChecklistItem

/**
 * Hardcoded patrol areas and checklist data for Port Stewart, Cape York.
 * Ports iOS PortStewartZones.
 */
object PortStewartZones {

    data class AreaCoordinate(val latitude: Double, val longitude: Double)

    val areaCoordinates: Map<String, AreaCoordinate> = mapOf(
        "North Beach Dunes"        to AreaCoordinate(-14.677, 143.702),
        "River Mouth Flats"        to AreaCoordinate(-14.711, 143.722),
        "Camping Ground Perimeter" to AreaCoordinate(-14.700, 143.699),
        "Airstrip Corridor"        to AreaCoordinate(-14.720, 143.690),
        "Southern Scrub Belt"      to AreaCoordinate(-14.740, 143.703),
        "Creek Line East"          to AreaCoordinate(-14.708, 143.730),
        "Creek Line West"          to AreaCoordinate(-14.708, 143.678),
        "Headland Track"           to AreaCoordinate(-14.688, 143.718),
        "Mangrove Edge"            to AreaCoordinate(-14.728, 143.712),
        "Central Clearing"         to AreaCoordinate(-14.710, 143.700),
    )

    val patrolAreas: List<String> = listOf(
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

    val defaultChecklist: List<PatrolChecklistItem> = listOf(
        PatrolChecklistItem(label = "Check GPS is recording"),
        PatrolChecklistItem(label = "Photograph new infestations"),
        PatrolChecklistItem(label = "Record all Lantana sightings"),
        PatrolChecklistItem(label = "Check previous treatment sites"),
        PatrolChecklistItem(label = "Note regrowth on treated plants"),
        PatrolChecklistItem(label = "Check pesticide supply before departing")
    )

    fun defaultChecklist(area: String): List<PatrolChecklistItem> {
        val items = defaultChecklist.toMutableList()
        items.add(0, PatrolChecklistItem(label = "Walk full boundary of $area"))
        return items
    }
}
