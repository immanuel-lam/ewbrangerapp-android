package org.yac.llamarangers.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.dao.RangerTaskDao
import org.yac.llamarangers.data.local.dao.TreatmentRecordDao
import org.yac.llamarangers.data.local.dao.SyncQueueDao
import org.yac.llamarangers.data.local.db.AppDatabase
import org.yac.llamarangers.data.local.entity.RangerTaskEntity
import org.yac.llamarangers.data.local.entity.SyncQueueEntity
import org.yac.llamarangers.domain.model.enums.SyncStatus
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val db: AppDatabase,
    private val taskDao: RangerTaskDao,
    private val treatmentDao: TreatmentRecordDao,
    private val syncQueueDao: SyncQueueDao
) {

    fun observeAllTasks(): Flow<List<RangerTaskEntity>> =
        taskDao.observeAll()

    fun observeTasksForRanger(rangerId: String): Flow<List<RangerTaskEntity>> =
        taskDao.observeByRangerId(rangerId)

    suspend fun fetchAllTasks(rangerId: String? = null): List<RangerTaskEntity> =
        if (rangerId != null) {
            taskDao.fetchByRangerId(rangerId)
        } else {
            taskDao.fetchAll()
        }

    suspend fun createTask(
        title: String,
        notes: String?,
        priority: String,
        dueDate: Long?,
        rangerId: String
    ): RangerTaskEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()

        val entity = RangerTaskEntity(
            id = id,
            createdAt = now,
            updatedAt = now,
            title = title,
            notes = notes,
            priority = priority,
            dueDate = dueDate,
            isComplete = false,
            completedAt = null,
            syncStatus = SyncStatus.PENDING_CREATE.value,
            assignedRangerId = rangerId,
            sourceTreatmentId = null
        )

        db.withTransaction {
            taskDao.upsert(entity)

            syncQueueDao.upsert(
                SyncQueueEntity(
                    id = UUID.randomUUID().toString(),
                    createdAt = now,
                    entityName = "RangerTask",
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

    /**
     * Auto-create a follow-up task from a treatment's followUpDate.
     * No-ops if the treatment has no followUpDate or already has a linked task.
     */
    suspend fun createFollowUpTask(
        treatmentId: String,
        treatmentMethod: String,
        followUpDate: Long,
        rangerId: String
    ): RangerTaskEntity? {
        // Check if a follow-up task already exists for this treatment
        val existing = taskDao.fetchBySourceTreatmentId(treatmentId)
        if (existing != null) return null

        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val methodDisplay = treatmentMethod.replaceFirstChar { it.uppercase() }

        val entity = RangerTaskEntity(
            id = id,
            createdAt = now,
            updatedAt = now,
            title = "Regrowth check \u2014 $methodDisplay",
            notes = "Check treatment site for regrowth. Linked to $methodDisplay treatment.",
            priority = "medium",
            dueDate = followUpDate,
            isComplete = false,
            completedAt = null,
            syncStatus = SyncStatus.PENDING_CREATE.value,
            assignedRangerId = rangerId,
            sourceTreatmentId = treatmentId
        )

        db.withTransaction {
            taskDao.upsert(entity)

            syncQueueDao.upsert(
                SyncQueueEntity(
                    id = UUID.randomUUID().toString(),
                    createdAt = now,
                    entityName = "RangerTask",
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

    suspend fun toggleComplete(taskId: String) {
        val now = System.currentTimeMillis()
        db.withTransaction {
            val task = taskDao.findById(taskId) ?: return@withTransaction
            val newComplete = !task.isComplete
            val completedAt = if (newComplete) now else null

            taskDao.updateCompletion(taskId, newComplete, completedAt, now, SyncStatus.PENDING_UPDATE.value)

            syncQueueDao.upsert(
                SyncQueueEntity(
                    id = UUID.randomUUID().toString(),
                    createdAt = now,
                    entityName = "RangerTask",
                    entityId = taskId,
                    operationType = "update",
                    payload = null,
                    attemptCount = 0,
                    lastAttemptAt = null,
                    lastErrorMessage = null
                )
            )
        }
    }

    suspend fun updateTask(
        taskId: String,
        title: String,
        notes: String?,
        priority: String,
        dueDate: Long?
    ) {
        val now = System.currentTimeMillis()
        db.withTransaction {
            taskDao.updateTask(taskId, title, notes, priority, dueDate, now, SyncStatus.PENDING_UPDATE.value)

            syncQueueDao.upsert(
                SyncQueueEntity(
                    id = UUID.randomUUID().toString(),
                    createdAt = now,
                    entityName = "RangerTask",
                    entityId = taskId,
                    operationType = "update",
                    payload = null,
                    attemptCount = 0,
                    lastAttemptAt = null,
                    lastErrorMessage = null
                )
            )
        }
    }

    suspend fun deleteTask(taskId: String) {
        taskDao.deleteById(taskId)
    }
}
