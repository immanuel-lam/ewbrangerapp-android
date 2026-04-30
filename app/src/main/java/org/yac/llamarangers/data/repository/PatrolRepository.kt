package org.yac.llamarangers.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.dao.PatrolRecordDao
import org.yac.llamarangers.data.local.dao.SyncQueueDao
import org.yac.llamarangers.data.local.db.AppDatabase
import org.yac.llamarangers.data.local.entity.PatrolRecordEntity
import org.yac.llamarangers.data.local.entity.SyncQueueEntity
import org.yac.llamarangers.domain.model.enums.SyncStatus
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatrolRepository @Inject constructor(
    private val db: AppDatabase,
    private val patrolDao: PatrolRecordDao,
    private val syncQueueDao: SyncQueueDao
) {

    fun observeAllPatrols(): Flow<List<PatrolRecordEntity>> =
        patrolDao.observeAll()

    suspend fun fetchAllPatrols(): List<PatrolRecordEntity> =
        patrolDao.fetchAll()

    suspend fun fetchActivePatrol(rangerId: String): PatrolRecordEntity? =
        patrolDao.fetchActivePatrol(rangerId)

    suspend fun createPatrol(
        areaName: String,
        rangerId: String,
        checklistItemsJson: String?
    ): PatrolRecordEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()

        val entity = PatrolRecordEntity(
            id = id,
            createdAt = now,
            updatedAt = now,
            patrolDate = now,
            startTime = now,
            endTime = null,
            areaName = areaName,
            checklistItems = checklistItemsJson ?: "[]",
            notes = "",
            syncStatus = SyncStatus.PENDING_CREATE.value,
            rangerId = rangerId
        )

        db.withTransaction {
            patrolDao.upsert(entity)

            syncQueueDao.upsert(
                SyncQueueEntity(
                    id = UUID.randomUUID().toString(),
                    createdAt = now,
                    entityName = "PatrolRecord",
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

    suspend fun updateChecklist(patrolId: String, checklistItemsJson: String) {
        val now = System.currentTimeMillis()
        db.withTransaction {
            patrolDao.updateChecklist(patrolId, checklistItemsJson, now, SyncStatus.PENDING_UPDATE.value)

            syncQueueDao.upsert(
                SyncQueueEntity(
                    id = UUID.randomUUID().toString(),
                    createdAt = now,
                    entityName = "PatrolRecord",
                    entityId = patrolId,
                    operationType = "update",
                    payload = null,
                    attemptCount = 0,
                    lastAttemptAt = null,
                    lastErrorMessage = null
                )
            )
        }
    }

    suspend fun finishPatrol(patrolId: String) {
        val now = System.currentTimeMillis()
        db.withTransaction {
            patrolDao.finishPatrol(patrolId, now, now, SyncStatus.PENDING_UPDATE.value)

            syncQueueDao.upsert(
                SyncQueueEntity(
                    id = UUID.randomUUID().toString(),
                    createdAt = now,
                    entityName = "PatrolRecord",
                    entityId = patrolId,
                    operationType = "update",
                    payload = null,
                    attemptCount = 0,
                    lastAttemptAt = null,
                    lastErrorMessage = null
                )
            )
        }
    }

    suspend fun deletePatrol(patrolId: String) {
        patrolDao.deleteById(patrolId)
    }
}
