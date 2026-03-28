package org.yac.llamarangers.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.entity.SyncQueueEntity

@Dao
interface SyncQueueDao {

    @Query("SELECT * FROM sync_queue ORDER BY created_at ASC")
    fun observeAll(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue ORDER BY created_at ASC")
    suspend fun fetchAll(): List<SyncQueueEntity>

    @Upsert
    suspend fun upsert(entity: SyncQueueEntity)

    @Upsert
    suspend fun upsertAll(entities: List<SyncQueueEntity>)

    @Delete
    suspend fun delete(entity: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM sync_queue")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM sync_queue")
    suspend fun count(): Int
}
