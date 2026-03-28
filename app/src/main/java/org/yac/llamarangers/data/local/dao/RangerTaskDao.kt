package org.yac.llamarangers.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.entity.RangerTaskEntity

@Dao
interface RangerTaskDao {

    @Query("SELECT * FROM ranger_task ORDER BY due_date ASC")
    fun observeAll(): Flow<List<RangerTaskEntity>>

    @Query("SELECT * FROM ranger_task ORDER BY due_date ASC")
    suspend fun fetchAll(): List<RangerTaskEntity>

    @Query("SELECT * FROM ranger_task WHERE id = :id")
    suspend fun findById(id: String): RangerTaskEntity?

    @Upsert
    suspend fun upsert(entity: RangerTaskEntity)

    @Upsert
    suspend fun upsertAll(entities: List<RangerTaskEntity>)

    @Delete
    suspend fun delete(entity: RangerTaskEntity)

    @Query("DELETE FROM ranger_task WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM ranger_task WHERE assigned_ranger_id = :rangerId ORDER BY due_date ASC")
    suspend fun fetchByRangerId(rangerId: String): List<RangerTaskEntity>

    @Query("SELECT * FROM ranger_task WHERE assigned_ranger_id = :rangerId ORDER BY due_date ASC")
    fun observeByRangerId(rangerId: String): Flow<List<RangerTaskEntity>>

    @Query("SELECT * FROM ranger_task WHERE is_complete = 0 ORDER BY due_date ASC")
    fun observeIncomplete(): Flow<List<RangerTaskEntity>>

    @Query("SELECT * FROM ranger_task WHERE is_complete = 0 ORDER BY due_date ASC")
    suspend fun fetchIncomplete(): List<RangerTaskEntity>

    @Query("SELECT * FROM ranger_task WHERE source_treatment_id = :treatmentId")
    suspend fun fetchBySourceTreatmentId(treatmentId: String): RangerTaskEntity?

    @Query("SELECT * FROM ranger_task WHERE sync_status != 3 ORDER BY created_at ASC")
    suspend fun fetchUnsynced(): List<RangerTaskEntity>

    @Query("SELECT COUNT(*) FROM ranger_task")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM ranger_task WHERE is_complete = 0")
    suspend fun countIncomplete(): Int

    @Query("UPDATE ranger_task SET is_complete = :isComplete, completed_at = :completedAt, updated_at = :updatedAt, sync_status = :syncStatus WHERE id = :id")
    suspend fun updateCompletion(id: String, isComplete: Boolean, completedAt: Long?, updatedAt: Long, syncStatus: Int)

    @Query("UPDATE ranger_task SET title = :title, notes = :notes, priority = :priority, due_date = :dueDate, updated_at = :updatedAt, sync_status = :syncStatus WHERE id = :id")
    suspend fun updateTask(id: String, title: String, notes: String?, priority: String, dueDate: Long?, updatedAt: Long, syncStatus: Int)

    @Query("DELETE FROM ranger_task")
    suspend fun deleteAll()
}
