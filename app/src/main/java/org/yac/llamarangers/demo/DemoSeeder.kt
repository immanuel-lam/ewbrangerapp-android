package org.yac.llamarangers.demo

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import org.yac.llamarangers.data.local.dao.InfestationZoneDao
import org.yac.llamarangers.data.local.dao.InfestationZoneSnapshotDao
import org.yac.llamarangers.data.local.dao.PatrolRecordDao
import org.yac.llamarangers.data.local.dao.PesticideStockDao
import org.yac.llamarangers.data.local.dao.PesticideUsageRecordDao
import org.yac.llamarangers.data.local.dao.RangerProfileDao
import org.yac.llamarangers.data.local.dao.RangerTaskDao
import org.yac.llamarangers.data.local.dao.SightingLogDao
import org.yac.llamarangers.data.local.dao.TreatmentRecordDao
import org.yac.llamarangers.data.local.entity.InfestationZoneEntity
import org.yac.llamarangers.data.local.entity.InfestationZoneSnapshotEntity
import org.yac.llamarangers.data.local.entity.PatrolRecordEntity
import org.yac.llamarangers.data.local.entity.PesticideStockEntity
import org.yac.llamarangers.data.local.entity.PesticideUsageRecordEntity
import org.yac.llamarangers.data.local.entity.RangerProfileEntity
import org.yac.llamarangers.data.local.entity.RangerTaskEntity
import org.yac.llamarangers.data.local.entity.SightingLogEntity
import org.yac.llamarangers.data.local.entity.TreatmentRecordEntity
import org.yac.llamarangers.domain.model.enums.RangerRole
import org.yac.llamarangers.domain.model.enums.SyncStatus
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds rich fake data for the demo build. Idempotent -- guarded by a SharedPreferences flag.
 */
@Singleton
class DemoSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rangerProfileDao: RangerProfileDao,
    private val sightingLogDao: SightingLogDao,
    private val treatmentRecordDao: TreatmentRecordDao,
    private val infestationZoneDao: InfestationZoneDao,
    private val infestationZoneSnapshotDao: InfestationZoneSnapshotDao,
    private val patrolRecordDao: PatrolRecordDao,
    private val pesticideStockDao: PesticideStockDao,
    private val pesticideUsageRecordDao: PesticideUsageRecordDao,
    private val rangerTaskDao: RangerTaskDao,
) {

    private val prefs: SharedPreferences
        get() = context.getSharedPreferences("demo_seeder", Context.MODE_PRIVATE)

    private val synced = SyncStatus.SYNCED.value

    // ----------------------------------------------------------------
    // Public entry points
    // ----------------------------------------------------------------

    /** Seed the three demo rangers if the ranger table is empty. */
    suspend fun seedRangersIfNeeded() {
        if (rangerProfileDao.count() > 0) return
        val now = System.currentTimeMillis()
        val demoData = listOf(
            Triple("Alice Johnson", RangerRole.SENIOR_RANGER.value, uuid()),
            Triple("Bob Smith", RangerRole.RANGER.value, uuid()),
            Triple("Carol White", RangerRole.RANGER.value, uuid()),
        )
        for ((name, role, id) in demoData) {
            rangerProfileDao.upsert(
                RangerProfileEntity(
                    id = id,
                    createdAt = now,
                    updatedAt = now,
                    supabaseUid = uuid(),
                    displayName = name,
                    role = role,
                    isCurrentDevice = false,
                    avatarFilename = null,
                    syncStatus = synced,
                )
            )
        }
    }

    /** Seed all rich demo data (sightings, treatments, zones, etc.). Runs once. */
    suspend fun seed() {
        if (prefs.getBoolean(SEEDED_KEY, false)) return

        val rangers = rangerProfileDao.fetchAll()
        if (rangers.size < 3) return

        val alice = rangers.firstOrNull { it.displayName == "Alice Johnson" } ?: rangers[0]
        val bob = rangers.firstOrNull { it.displayName == "Bob Smith" } ?: rangers[1]
        val carol = rangers.firstOrNull { it.displayName == "Carol White" } ?: rangers[2]

        val variantPhotos = seedPhotos()

        // --------------------------------------------------------
        // ZONES
        // --------------------------------------------------------
        data class ZoneSpec(
            val name: String,
            val status: String,
            val variant: String,
            val lat: Double,
            val lon: Double,
        )

        val zoneSpecs = listOf(
            ZoneSpec("North Creek Gully", "active", "lantana", -14.685, 143.712),
            ZoneSpec("Boundary Road East", "underTreatment", "rubberVine", -14.718, 143.698),
            ZoneSpec("Homestead Track", "cleared", "pricklyAcacia", -14.703, 143.722),
            ZoneSpec("Rocky Point Scrub", "active", "giantRatsTailGrass", -14.695, 143.683),
            ZoneSpec("Mangrove Flat", "underTreatment", "pondApple", -14.725, 143.715),
            ZoneSpec("Station Dam", "cleared", "sicklepod", -14.710, 143.730),
        )

        val zoneIds = mutableListOf<String>()
        for (spec in zoneSpecs) {
            val zoneId = uuid()
            zoneIds.add(zoneId)

            infestationZoneDao.upsert(
                InfestationZoneEntity(
                    id = zoneId,
                    createdAt = ago(days = 180),
                    updatedAt = ago(days = 7),
                    name = spec.name,
                    status = spec.status,
                    dominantVariant = spec.variant,
                    syncStatus = synced,
                )
            )

            // Diamond polygon around centroid (~300 m radius)
            val d = 0.003
            infestationZoneSnapshotDao.upsert(
                InfestationZoneSnapshotEntity(
                    id = uuid(),
                    snapshotDate = ago(days = 30),
                    createdByRangerId = alice.id,
                    area = 90_000.0, // ~9 ha
                    polygonCoordinates = listOf(
                        listOf(spec.lat + d, spec.lon),
                        listOf(spec.lat, spec.lon + d),
                        listOf(spec.lat - d, spec.lon),
                        listOf(spec.lat, spec.lon - d),
                    ),
                    zoneId = zoneId,
                )
            )
        }

        // --------------------------------------------------------
        // SIGHTINGS  (28 entries spread over 6 months)
        // --------------------------------------------------------
        data class SightingSpec(
            val ranger: RangerProfileEntity,
            val variant: String,
            val size: String,
            val lat: Double,
            val lon: Double,
            val daysAgo: Int,
            val zoneIdx: Int?,
        )

        val sightingSpecs = listOf(
            // Zone 0 -- North Creek Gully (lantana)  centroid (-14.685, 143.712)
            SightingSpec(alice, "lantana", "large", -14.685, 143.712, 168, 0),
            SightingSpec(carol, "lantana", "large", -14.684, 143.713, 134, 0),
            SightingSpec(bob, "lantana", "large", -14.686, 143.713, 99, 0),
            SightingSpec(alice, "lantana", "small", -14.684, 143.711, 64, 0),
            SightingSpec(alice, "lantana", "medium", -14.686, 143.711, 22, 0),
            SightingSpec(alice, "lantana", "medium", -14.685, 143.7135, 2, 0),
            // Zone 1 -- Boundary Road East (rubberVine)  centroid (-14.718, 143.698)
            SightingSpec(bob, "rubberVine", "medium", -14.718, 143.698, 162, 1),
            SightingSpec(alice, "rubberVine", "large", -14.717, 143.699, 127, 1),
            SightingSpec(carol, "rubberVine", "medium", -14.719, 143.699, 92, 1),
            SightingSpec(carol, "rubberVine", "large", -14.717, 143.697, 50, 1),
            SightingSpec(carol, "rubberVine", "medium", -14.719, 143.697, 14, 1),
            // Zone 2 -- Homestead Track (pricklyAcacia)    centroid (-14.703, 143.722)
            SightingSpec(carol, "pricklyAcacia", "small", -14.703, 143.722, 155, 2),
            SightingSpec(alice, "pricklyAcacia", "medium", -14.702, 143.7225, 106, 2),
            SightingSpec(bob, "pricklyAcacia", "medium", -14.704, 143.7225, 57, 2),
            SightingSpec(bob, "pricklyAcacia", "small", -14.703, 143.721, 18, 2),
            // Zone 3 -- Rocky Point Scrub (giantRatsTailGrass)  centroid (-14.695, 143.683)
            SightingSpec(alice, "giantRatsTailGrass", "medium", -14.695, 143.683, 148, 3),
            SightingSpec(bob, "giantRatsTailGrass", "medium", -14.694, 143.684, 120, 3),
            SightingSpec(bob, "giantRatsTailGrass", "medium", -14.696, 143.6825, 78, 3),
            SightingSpec(alice, "giantRatsTailGrass", "medium", -14.6945, 143.682, 43, 3),
            SightingSpec(alice, "giantRatsTailGrass", "large", -14.6955, 143.684, 10, 3),
            // Zone 4 -- Mangrove Flat (pondApple)      centroid (-14.725, 143.715)
            SightingSpec(bob, "pondApple", "small", -14.725, 143.715, 141, 4),
            SightingSpec(alice, "pondApple", "small", -14.724, 143.7155, 85, 4),
            SightingSpec(bob, "pondApple", "small", -14.726, 143.7145, 36, 4),
            SightingSpec(bob, "pondApple", "medium", -14.7245, 143.714, 7, 4),
            // Zone 5 -- Station Dam (sicklepod)        centroid (-14.710, 143.730)
            SightingSpec(carol, "sicklepod", "small", -14.710, 143.730, 113, 5),
            SightingSpec(carol, "sicklepod", "large", -14.709, 143.731, 71, 5),
            SightingSpec(carol, "sicklepod", "large", -14.711, 143.729, 29, 5),
            SightingSpec(carol, "sicklepod", "small", -14.709, 143.729, 4, 5),
        )

        data class SightingRef(val id: String, val rangerId: String, val createdAt: Long)

        val sightingRefs = mutableListOf<SightingRef>()
        for (spec in sightingSpecs) {
            val id = uuid()
            val date = ago(days = spec.daysAgo)
            sightingLogDao.upsert(
                SightingLogEntity(
                    id = id,
                    createdAt = date,
                    updatedAt = date,
                    latitude = spec.lat,
                    longitude = spec.lon,
                    horizontalAccuracy = 8.0,
                    variant = spec.variant,
                    infestationSize = spec.size,
                    infestationAreaEstimate = null,
                    notes = null,
                    photoFilenames = variantPhotos[spec.variant] ?: emptyList(),
                    deviceId = "demo-device",
                    serverId = null,
                    voiceNotePath = null,
                    syncStatus = synced,
                    rangerId = spec.ranger.id,
                    infestationZoneId = spec.zoneIdx?.let { zoneIds[it] },
                )
            )
            sightingRefs.add(SightingRef(id, spec.ranger.id, date))
        }

        // --------------------------------------------------------
        // TREATMENT RECORDS  (first 18 sightings)
        // --------------------------------------------------------
        val methods = listOf("cutStump", "splatGun", "foliarSpray", "basalBark", "mechanical", "stemInjection")
        val products = listOf("Garlon 600", "Access", "Glyphosate 360", "Tordon 75-D", "Starane Advanced")
        val outcomes = listOf(
            "Cut stumps painted immediately, good coverage achieved.",
            "Foliar spray applied across canopy. Regrowth expected in 60 days.",
            "Basal bark treatment on all stems >5 cm diameter.",
            "Splat gun applied to accessible stems, 95% coverage.",
            "Full canopy foliar treatment. Follow-up scheduled at 60 days.",
        )

        for ((i, sRef) in sightingRefs.take(18).withIndex()) {
            val date = sRef.createdAt + 7_200_000L // +2 hours in millis
            val followUp = if (i % 4 == 0) date + 60L * DAY_MILLIS else null
            treatmentRecordDao.upsert(
                TreatmentRecordEntity(
                    id = uuid(),
                    createdAt = date,
                    updatedAt = date,
                    treatmentDate = date,
                    method = methods[i % methods.size],
                    herbicideProduct = products[i % products.size],
                    outcomeNotes = outcomes[i % outcomes.size],
                    followUpDate = followUp,
                    photoFilenames = emptyList(),
                    syncStatus = synced,
                    sightingId = sRef.id,
                    rangerId = sRef.rangerId,
                    followUpTaskId = null,
                )
            )
        }

        // --------------------------------------------------------
        // PATROL RECORDS  (10 patrols across the past 3 weeks)
        // --------------------------------------------------------
        data class PatrolSpec(
            val ranger: RangerProfileEntity,
            val area: String,
            val daysAgo: Int,
        )

        val patrolSpecs = listOf(
            PatrolSpec(alice, "North Beach Dunes", 3),
            PatrolSpec(bob, "Creek Line East", 3),
            PatrolSpec(carol, "Central Clearing", 4),
            PatrolSpec(alice, "Headland Track", 7),
            PatrolSpec(bob, "Mangrove Edge", 7),
            PatrolSpec(carol, "North Beach Dunes", 10),
            PatrolSpec(alice, "River Mouth Flats", 14),
            PatrolSpec(bob, "Airstrip Corridor", 14),
            PatrolSpec(carol, "Camping Ground Perimeter", 17),
            PatrolSpec(alice, "Southern Scrub Belt", 21),
        )

        for (spec in patrolSpecs) {
            val start = ago(days = spec.daysAgo)
            val endTime = start + 10_800_000L // 3 hour patrol

            val checklistJson = buildChecklistJson(start)

            patrolRecordDao.upsert(
                PatrolRecordEntity(
                    id = uuid(),
                    createdAt = start,
                    updatedAt = start,
                    patrolDate = start,
                    startTime = start,
                    endTime = endTime,
                    areaName = spec.area,
                    checklistItems = checklistJson,
                    notes = "Patrol complete. All visible invasive plants logged and GPS-tagged.",
                    syncStatus = synced,
                    rangerId = spec.ranger.id,
                )
            )
        }

        // --------------------------------------------------------
        // PESTICIDE STOCK + USAGE
        // --------------------------------------------------------
        data class StockSpec(
            val name: String,
            val unit: String,
            val qty: Double,
            val threshold: Double,
        )

        val stockSpecs = listOf(
            StockSpec("Garlon 600", "litres", 8.5, 2.0),
            StockSpec("Access", "litres", 4.2, 1.0),
            StockSpec("Glyphosate 360", "litres", 12.0, 3.0),
        )

        val stockIds = mutableListOf<String>()
        for (spec in stockSpecs) {
            val id = uuid()
            stockIds.add(id)
            pesticideStockDao.upsert(
                PesticideStockEntity(
                    id = id,
                    createdAt = ago(days = 180),
                    updatedAt = ago(days = 2),
                    productName = spec.name,
                    unit = spec.unit,
                    currentQuantity = spec.qty,
                    minThreshold = spec.threshold,
                    syncStatus = synced,
                )
            )
        }

        data class UsageSpec(
            val stockIdx: Int,
            val qty: Double,
            val daysAgo: Int,
            val ranger: RangerProfileEntity,
        )

        val usageSpecs = listOf(
            UsageSpec(0, 0.5, 3, alice), UsageSpec(0, 0.8, 7, bob), UsageSpec(0, 0.3, 14, carol),
            UsageSpec(0, 1.2, 21, alice), UsageSpec(1, 0.4, 4, bob), UsageSpec(1, 0.6, 10, carol),
            UsageSpec(1, 0.3, 18, alice), UsageSpec(2, 1.5, 5, bob), UsageSpec(2, 0.9, 12, carol),
            UsageSpec(2, 2.0, 25, alice), UsageSpec(2, 0.7, 35, bob),
        )

        for (spec in usageSpecs) {
            val date = ago(days = spec.daysAgo)
            pesticideUsageRecordDao.upsert(
                PesticideUsageRecordEntity(
                    id = uuid(),
                    createdAt = date,
                    updatedAt = date,
                    usedQuantity = spec.qty,
                    usedAt = date,
                    notes = null,
                    syncStatus = synced,
                    stockId = stockIds[spec.stockIdx],
                    treatmentId = null,
                    rangerId = spec.ranger.id,
                )
            )
        }

        // --------------------------------------------------------
        // RANGER TASKS
        // --------------------------------------------------------
        data class TaskSpec(
            val title: String,
            val ranger: RangerProfileEntity,
            val priority: String,
            val complete: Boolean,
            val daysAgo: Int,
        )

        val taskSpecs = listOf(
            TaskSpec("Lantana regrowth check \u2014 North Creek Gully", alice, "high", false, 14),
            TaskSpec("Rubber vine follow-up spray \u2014 Boundary Road", bob, "high", false, 7),
            TaskSpec("Photo documentation \u2014 Station Dam sicklepod", carol, "medium", true, 21),
            TaskSpec("Restock Garlon 600 and Tordon 75-D", alice, "medium", false, 3),
            TaskSpec("Check lantana biocontrol release site", bob, "low", false, 10),
            TaskSpec("Update zone polygons after wet season rain", carol, "medium", true, 18),
            TaskSpec("Pond apple mechanical removal \u2014 Mangrove Flat", alice, "high", false, 2),
        )

        for (spec in taskSpecs) {
            val date = ago(days = spec.daysAgo)
            rangerTaskDao.upsert(
                RangerTaskEntity(
                    id = uuid(),
                    createdAt = date,
                    updatedAt = date,
                    title = spec.title,
                    notes = null,
                    priority = spec.priority,
                    dueDate = null,
                    isComplete = spec.complete,
                    completedAt = if (spec.complete) date else null,
                    syncStatus = synced,
                    assignedRangerId = spec.ranger.id,
                    sourceTreatmentId = null,
                )
            )
        }

        // Mark as seeded and fake a recent cloud sync timestamp
        prefs.edit()
            .putBoolean(SEEDED_KEY, true)
            .putLong("lastSyncTimestamp", System.currentTimeMillis() - 3_600_000L)
            .apply()
    }

    // ----------------------------------------------------------------
    // Photo helpers
    // ----------------------------------------------------------------

    /**
     * Copies each bundled demo_lantana_N asset to internal Photos/ dir once.
     * Returns a map of variant string to list of filenames to attach to sightings.
     */
    private fun seedPhotos(): Map<String, List<String>> {
        val photosDir = File(context.filesDir, "Photos")
        if (!photosDir.exists()) photosDir.mkdirs()

        // asset name -> variant keys that should use it
        val assetMap = listOf(
            "demo_lantana_1" to listOf("lantana"),
            "demo_lantana_2" to listOf("rubberVine"),
            "demo_lantana_3" to listOf("pricklyAcacia"),
            "demo_lantana_4" to listOf("giantRatsTailGrass"),
            "demo_lantana_5" to listOf("pondApple"),
            "demo_lantana_6" to listOf("sicklepod", "unknown"),
        )

        val variantPhotos = mutableMapOf<String, List<String>>()
        for ((asset, variants) in assetMap) {
            val filename = "$asset.jpg"
            val outFile = File(photosDir, filename)
            if (!outFile.exists()) {
                try {
                    context.assets.open("demo/$filename").use { input ->
                        outFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (_: Exception) {
                    // Asset may not be bundled yet -- skip gracefully
                    continue
                }
            }
            for (variant in variants) {
                variantPhotos[variant] = listOf(filename)
            }
        }
        return variantPhotos
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private fun buildChecklistJson(startMillis: Long): String {
        // Matches the iOS PatrolChecklistItem JSON structure
        return """[
            {"label":"GPS unit charged","isComplete":true,"completedAt":$startMillis},
            {"label":"Herbicide mix prepared","isComplete":true,"completedAt":${startMillis + 600_000}},
            {"label":"Safety gear donned","isComplete":true,"completedAt":${startMillis + 900_000}},
            {"label":"Area perimeter walked","isComplete":true,"completedAt":${startMillis + 3_600_000}},
            {"label":"All sightings logged","isComplete":true,"completedAt":${startMillis + 9_000_000}}
        ]""".trimIndent()
    }

    private fun ago(days: Int): Long =
        System.currentTimeMillis() - days * DAY_MILLIS

    companion object {
        private const val SEEDED_KEY = "demoDataSeeded_v3"
        private val DAY_MILLIS = TimeUnit.DAYS.toMillis(1)
        private fun uuid(): String = UUID.randomUUID().toString()
    }
}
