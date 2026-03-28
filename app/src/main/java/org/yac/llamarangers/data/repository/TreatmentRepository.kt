package org.yac.llamarangers.data.repository

import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.dao.TreatmentRecordDao
import org.yac.llamarangers.data.local.dao.SyncQueueDao
import org.yac.llamarangers.data.local.entity.TreatmentRecordEntity
import org.yac.llamarangers.data.local.entity.SyncQueueEntity
import org.yac.llamarangers.domain.model.enums.SyncStatus
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TreatmentRepository @Inject constructor(
    private val treatmentDao: TreatmentRecordDao,
    private val syncQueueDao: SyncQueueDao
) {

    fun observeTreatmentsForSighting(sightingId: String): Flow<List<TreatmentRecordEntity>> =
        treatmentDao.observeBySightingId(sightingId)

    suspend fun fetchTreatmentsForSighting(sightingId: String): List<TreatmentRecordEntity> =
        treatmentDao.fetchBySightingId(sightingId)

    suspend fun addTreatment(
        sightingId: String,
        method: String,
        herbicideProduct: String?,
        outcomeNotes: String?,
        followUpDate: Long?,
        rangerId: String
    ): TreatmentRecordEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()

        val entity = TreatmentRecordEntity(
            id = id,
            createdAt = now,
            updatedAt = now,
            treatmentDate = now,
            method = method,
            herbicideProduct = herbicideProduct,
            outcomeNotes = outcomeNotes,
            followUpDate = followUpDate,
            photoFilenames = emptyList(),
            syncStatus = SyncStatus.PENDING_CREATE.value,
            sightingId = sightingId,
            rangerId = rangerId,
            followUpTaskId = null
        )

        treatmentDao.upsert(entity)

        syncQueueDao.upsert(
            SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                createdAt = now,
                entityName = "TreatmentRecord",
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
}
