package org.yac.llamarangers.data.repository

import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.dao.RangerProfileDao
import org.yac.llamarangers.data.local.dao.SyncQueueDao
import org.yac.llamarangers.data.local.entity.RangerProfileEntity
import org.yac.llamarangers.data.local.entity.SyncQueueEntity
import org.yac.llamarangers.domain.model.enums.RangerRole
import org.yac.llamarangers.domain.model.enums.SyncStatus
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RangerRepository @Inject constructor(
    private val rangerDao: RangerProfileDao,
    private val syncQueueDao: SyncQueueDao
) {

    fun observeAllRangers(): Flow<List<RangerProfileEntity>> =
        rangerDao.observeAll()

    suspend fun fetchAllRangers(): List<RangerProfileEntity> =
        rangerDao.fetchAll()

    suspend fun fetchCurrentRanger(): RangerProfileEntity? =
        rangerDao.fetchCurrentDevice()

    suspend fun createRanger(
        displayName: String,
        role: RangerRole,
        supabaseUid: String
    ): RangerProfileEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()

        val entity = RangerProfileEntity(
            id = id,
            createdAt = now,
            updatedAt = now,
            supabaseUid = supabaseUid,
            displayName = displayName,
            role = role.value,
            isCurrentDevice = false,
            avatarFilename = null,
            syncStatus = SyncStatus.PENDING_CREATE.value
        )

        rangerDao.upsert(entity)

        syncQueueDao.upsert(
            SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                createdAt = now,
                entityName = "RangerProfile",
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

    /**
     * Mark a single ranger as the current device user.
     * Clears the flag on all other rangers first.
     */
    suspend fun setCurrentDevice(rangerId: String) {
        rangerDao.clearAllCurrentDevice()
        rangerDao.setCurrentDevice(rangerId, true)
    }
}
