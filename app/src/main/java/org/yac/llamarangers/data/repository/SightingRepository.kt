package org.yac.llamarangers.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.dao.SightingLogDao
import org.yac.llamarangers.data.local.dao.RangerProfileDao
import org.yac.llamarangers.data.local.dao.SyncQueueDao
import org.yac.llamarangers.data.local.db.AppDatabase
import org.yac.llamarangers.data.local.entity.SightingLogEntity
import org.yac.llamarangers.data.local.entity.SyncQueueEntity
import org.yac.llamarangers.domain.model.enums.InfestationSize
import org.yac.llamarangers.domain.model.enums.LantanaVariant
import org.yac.llamarangers.domain.model.enums.SyncStatus
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SightingRepository @Inject constructor(
    private val db: AppDatabase,
    private val sightingDao: SightingLogDao,
    private val rangerDao: RangerProfileDao,
    private val syncQueueDao: SyncQueueDao
) {

    fun observeAllSightings(): Flow<List<SightingLogEntity>> =
        sightingDao.observeAll()

    suspend fun fetchAllSightings(): List<SightingLogEntity> =
        sightingDao.fetchAll()

    suspend fun fetchSightingsSince(epochMillis: Long): List<SightingLogEntity> =
        sightingDao.fetchSince(epochMillis)

    suspend fun createSighting(
        latitude: Double,
        longitude: Double,
        horizontalAccuracy: Double,
        variant: LantanaVariant,
        infestationSize: InfestationSize,
        notes: String?,
        photoFilenames: List<String>,
        rangerId: String,
        deviceId: String
    ): SightingLogEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()

        val entity = SightingLogEntity(
            id = id,
            createdAt = now,
            updatedAt = now,
            latitude = latitude,
            longitude = longitude,
            horizontalAccuracy = horizontalAccuracy,
            variant = variant.value,
            infestationSize = infestationSize.value,
            notes = notes,
            photoFilenames = photoFilenames,
            deviceId = deviceId,
            serverId = null,
            syncStatus = SyncStatus.PENDING_CREATE.value,
            rangerId = rangerId,
            infestationZoneId = null
        )

        db.withTransaction {
            sightingDao.upsert(entity)

            syncQueueDao.upsert(
                SyncQueueEntity(
                    id = UUID.randomUUID().toString(),
                    createdAt = now,
                    entityName = "SightingLog",
                    entityId = id,
                    operationType = "create",
                    payload = null,
                    attemptCount = 0,
                    lastAttemptAt = null,
                    lastErrorMessage = null
                )
            )
        }

        return entity
    }

    suspend fun deleteSighting(sightingId: String) {
        sightingDao.deleteById(sightingId)
    }
}
