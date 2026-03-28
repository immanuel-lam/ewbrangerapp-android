package org.yac.llamarangers.data.repository

import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.dao.InfestationZoneDao
import org.yac.llamarangers.data.local.dao.InfestationZoneSnapshotDao
import org.yac.llamarangers.data.local.dao.SightingLogDao
import org.yac.llamarangers.data.local.dao.SyncQueueDao
import org.yac.llamarangers.data.local.entity.InfestationZoneEntity
import org.yac.llamarangers.data.local.entity.InfestationZoneSnapshotEntity
import org.yac.llamarangers.data.local.entity.SightingLogEntity
import org.yac.llamarangers.data.local.entity.SyncQueueEntity
import org.yac.llamarangers.domain.model.enums.LantanaVariant
import org.yac.llamarangers.domain.model.enums.SyncStatus
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZoneRepository @Inject constructor(
    private val zoneDao: InfestationZoneDao,
    private val snapshotDao: InfestationZoneSnapshotDao,
    private val sightingDao: SightingLogDao,
    private val syncQueueDao: SyncQueueDao
) {

    fun observeAllZones(): Flow<List<InfestationZoneEntity>> =
        zoneDao.observeAll()

    suspend fun fetchAllZones(): List<InfestationZoneEntity> =
        zoneDao.fetchAll()

    suspend fun createZone(
        name: String?,
        dominantVariant: LantanaVariant
    ): InfestationZoneEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()

        val entity = InfestationZoneEntity(
            id = id,
            createdAt = now,
            updatedAt = now,
            name = name,
            status = "active",
            dominantVariant = dominantVariant.value,
            syncStatus = SyncStatus.PENDING_CREATE.value
        )

        zoneDao.upsert(entity)

        syncQueueDao.upsert(
            SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                createdAt = now,
                entityName = "InfestationZone",
                entityId = id,
                operationType = "create",
                payload = null,
                attemptCount = 0,
                lastAttemptAt = null,
                lastErrorMessage = null
            )
        )

        return entity
    }

    suspend fun updateZone(
        zoneId: String,
        name: String?,
        dominantVariant: LantanaVariant,
        status: String
    ) {
        val now = System.currentTimeMillis()
        zoneDao.updateZone(zoneId, name, dominantVariant.value, status, now, SyncStatus.PENDING_UPDATE.value)

        syncQueueDao.upsert(
            SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                createdAt = now,
                entityName = "InfestationZone",
                entityId = zoneId,
                operationType = "update",
                payload = null,
                attemptCount = 0,
                lastAttemptAt = null,
                lastErrorMessage = null
            )
        )
    }

    suspend fun deleteZone(zoneId: String) {
        zoneDao.deleteById(zoneId)
    }

    /**
     * Assign or un-assign a sighting to a zone.
     * Pass null for [zoneId] to remove the assignment.
     */
    suspend fun assignSighting(sightingId: String, zoneId: String?) {
        val now = System.currentTimeMillis()
        sightingDao.updateZoneAssignment(sightingId, zoneId, now)
    }

    /**
     * Add a polygon snapshot to a zone.
     * [coordinates] is a list of [lat, lon] pairs serialised as List<List<Double>>.
     */
    suspend fun addSnapshot(
        zoneId: String,
        coordinates: List<List<Double>>,
        area: Double,
        createdByRangerId: String
    ): InfestationZoneSnapshotEntity {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val entity = InfestationZoneSnapshotEntity(
            id = id,
            snapshotDate = now,
            polygonCoordinates = coordinates,
            area = area,
            createdByRangerId = createdByRangerId,
            zoneId = zoneId
        )

        snapshotDao.upsert(entity)
        return entity
    }

    fun observeSnapshotsForZone(zoneId: String): Flow<List<InfestationZoneSnapshotEntity>> =
        snapshotDao.observeByZoneId(zoneId)

    suspend fun fetchSnapshotsForZone(zoneId: String): List<InfestationZoneSnapshotEntity> =
        snapshotDao.fetchByZoneId(zoneId)

    suspend fun fetchSightingsForZone(zoneId: String): List<SightingLogEntity> =
        sightingDao.fetchByZoneId(zoneId)
}
